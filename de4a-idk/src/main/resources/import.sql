
INSERT INTO IssuingAuthority (id, evidenceType, countryCode, iaLevelPath) VALUES (1, 'HigherEdCertificate','PT', 'nuts0/edu');
INSERT INTO IssuingAuthority (id, evidenceType, countryCode, iaLevelPath) VALUES (2, 'BirthCertificate','ES', 'nuts0/soc');
INSERT INTO IssuingAuthority (id, evidenceType, countryCode, iaLevelPath) VALUES (3, 'DoingBusinessAbroad','ES', 'nuts0/soc');
INSERT INTO AtuItem (id, id_issuingAuthority, atuLevel, atuPath, atuCode, atuName, atuLatinName) VALUES (1, 1, 'nuts0', 'nuts0/edu', 'ptUniversityOfLisbon', 'Universidad de Lisboa', 'Universidad de Lisboa');
INSERT INTO AtuItem (id, id_issuingAuthority, atuLevel, atuPath, atuCode, atuName, atuLatinName) VALUES (2, 2, 'nuts0', 'nuts0/soc', 'MPTFP-SGAD', 'Registro Civil', 'Registro Civil');
INSERT INTO AtuItem (id, id_issuingAuthority, atuLevel, atuPath, atuCode, atuName, atuLatinName) VALUES (3, 3, 'nuts0', 'nuts0/soc', 'MPTFP-SGAD', 'DBA_MOCK', 'DBA_MOCK');
INSERT INTO EvidenceService (id, countryCode, atuCode, canonicalEvidence, service, dataOwner, dataTransferor, redirectURL) VALUES (1, 'PT', 'ptUniversityOfLisbon', 'HigherEdCertificate', 'urn::de4a.eu:identifiers:service::9991:PT000000029:HighEdEdCertificate:1.0', 'iso6523-actorid-upis::9991:PT000000101', 'iso6523-actorid-upis::9991:PT000000029', 'https://fenix-edu-inesc.pt/usip');
INSERT INTO EvidenceService (id, countryCode, atuCode, canonicalEvidence, service, dataOwner, dataTransferor, redirectURL) VALUES (2, 'ES', 'MPTFP-SGAD', 'BirthCertificate', 'iso6523-actorid-upis:service::9921:ESS2833002E:BirthCertificate:1.0', 'iso6523-actorid-upis::9921:E00000000T', 'iso6523-actorid-upis::9921:ESS2833002E', 'http://localhost:8882/pid-owner/evidence');
INSERT INTO EvidenceService (id, countryCode, atuCode, canonicalEvidence, service, dataOwner, dataTransferor, redirectURL) VALUES (3, 'ES', 'MPTFP-SGAD', 'DoingBusinessAbroad', 'iso6523-actorid-upis:service::9921:ESS2833002E:DoingBusinessAbroad:1.0', 'iso6523-actorid-upis::9921:ESS2833002E', 'iso6523-actorid-upis::9921:ESS2833002E', 'https://intermediacionpp.redsara.es/servicios/SVD/RegistroCivil.ConsultaNacimiento');