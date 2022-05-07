package eu.de4a.connector.api.service;

import java.util.Locale;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.mime.CMimeType;
import com.helger.dcng.api.me.EMEProtocol;
import com.helger.dcng.api.rest.DCNGPayload;
import com.helger.dcng.webapi.as4.ApiPostLookupAndSend;
import com.helger.json.IJsonObject;
import com.helger.json.serialize.JsonWriterSettings;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.factory.IIdentifierFactory;
import com.helger.peppolid.factory.SimpleIdentifierFactory;
import eu.de4a.connector.config.DE4AConstants;
import eu.de4a.connector.dto.AS4MessageDTO;
import eu.de4a.connector.error.exceptions.ConnectorException;
import eu.de4a.connector.error.model.EExternalModuleError;
import eu.de4a.connector.error.model.EFamilyErrorType;
import eu.de4a.connector.error.model.ELayerError;
import eu.de4a.connector.error.model.ELogMessages;
import eu.de4a.connector.utils.DOMUtils;
import eu.de4a.connector.utils.KafkaClientWrapper;

@Service
public class AS4Service {
    private static final Logger LOGGER = LoggerFactory.getLogger(AS4Service.class);
    private static final IIdentifierFactory IF = SimpleIdentifierFactory.INSTANCE;

    // Tags for Json returned by the AS4 API - Strong change dependence
    private static final String JSON_TAG_SUCCESS = "success";
    private static final String JSON_TAG_RESPONSE = "response";
    private static final String JSON_TAG_RESULT_LOOKUP = "lookup-results";
    private static final String JSON_TAG_RESULT_SEND = "sending-results";
    private static final String JSON_TAG_EXCEPTION = "exception";
    private static final String JSON_TAG_MESSAGE = "message";

    /**
     * Invoke the message exchange via API
     * {@link com.helger.dcng.webapi.as4.ApiPostLookendAndSend}
     *
     * @param messageDTO
     * @return if message is successfully sent
     */
    public boolean sendMessage(@Nonnull final AS4MessageDTO messageDTO) {
        final IParticipantIdentifier sPI = IF.parseParticipantIdentifier(messageDTO.getSenderID().toLowerCase(Locale.ROOT));
        final IParticipantIdentifier rPI = IF.parseParticipantIdentifier(messageDTO.getReceiverID().toLowerCase(Locale.ROOT));
        final IDocumentTypeIdentifier aDocumentTypeID = IF.parseDocumentTypeIdentifier(messageDTO.getDocTypeID());
        final IProcessIdentifier aProcessID = IF.createProcessIdentifier(DE4AConstants.PROCESS_SCHEME, messageDTO.getProcessID());

        final ICommonsList<DCNGPayload> aPayloads = new CommonsArrayList<>();
        final DCNGPayload a = new DCNGPayload();
        a.setValue(DOMUtils.documentToByte(messageDTO.getMessage()));
        a.setMimeType(CMimeType.APPLICATION_XML.getAsString());
        aPayloads.add(a);

        KafkaClientWrapper.sendInfo(ELogMessages.LOG_AS4_MSG_SENT, sPI.getURIEncoded(),
                rPI.getURIEncoded(), aDocumentTypeID.getURIEncoded(), aProcessID.getURIEncoded());

        final IJsonObject aJson = ApiPostLookupAndSend.perform(sPI, rPI, aDocumentTypeID, aProcessID,
                EMEProtocol.AS4.getTransportProfileID(), aPayloads);
        //Process json response
        manageAs4SendingResult(aJson);

        return true;
    }

    /**
     *
     * After the AS4 message exchange via API
     * {@link com.helger.dcng.webapi.as4.ApiPosLookendAndSend.java}
     * The results object is managed here
     *
     * @param aJson - Execution results in json format from the dcng-web-api
     */
    private void manageAs4SendingResult(final IJsonObject aJson) {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("AS4 Sending result:\n {}",
                aJson.getAsJsonString (JsonWriterSettings.DEFAULT_SETTINGS_FORMATTED));

        // Base exception to be thrown
        final ConnectorException ex = new ConnectorException().withLayer(ELayerError.COMMUNICATIONS)
                .withFamily(EFamilyErrorType.AS4_ERROR_COMMUNICATION);

        if(!aJson.getAsValue(JSON_TAG_SUCCESS).getAsBoolean(false)) {
            //A problem occurs sending the AS4 message
            if(aJson.containsKey(JSON_TAG_EXCEPTION)) {
                final String message = aJson.get(JSON_TAG_EXCEPTION).getAsObject()
                        .get(JSON_TAG_MESSAGE).getAsValue().getAsString();
                throw ex.withModule(EExternalModuleError.AS4).withMessageArg(message);
            }

            final IJsonObject lookupResults = (IJsonObject) aJson.get(JSON_TAG_RESULT_LOOKUP);
            final IJsonObject sendResults = (IJsonObject) aJson.get(JSON_TAG_RESULT_SEND);
            if(!lookupResults.getAsValue(JSON_TAG_SUCCESS).getAsBoolean(false)) {
                    final String smpErrMsg;
                    if(lookupResults.containsKey(JSON_TAG_RESPONSE))
                        smpErrMsg = lookupResults.get(JSON_TAG_RESPONSE)
                                .getAsValue().getAsString();
                    else
                        smpErrMsg = "Found no matching SMP service metadata";
                    throw ex.withModule(EExternalModuleError.SMP) .withMessageArg(smpErrMsg);
            }
            if(!sendResults.getAsValue(JSON_TAG_SUCCESS).getAsBoolean(false))
                    throw ex.withModule(EExternalModuleError.AS4)
                        .withMessageArg("Error with AS4 communications");
        }
    }
}
