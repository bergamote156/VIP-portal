/* minimal DB with:
 * - one group/engine/resource
 * - no terms of use / publication prompt on first login
 * - hardcoded API key
 * Assumes a single admin user exists.
 */
UPDATE VIPUsers SET apikey='00000000000000000000000000',termsUse=NOW(),lastUpdatePublications=NOW() WHERE email=(SELECT email FROM VIPUsers ORDER BY email ASC LIMIT 1);
INSERT INTO VIPGroups (name,public) VALUES ('g1',1);
INSERT INTO VIPUsersGroups (email,groupname,role) VALUES ((SELECT email FROM VIPUsers ORDER BY email ASC LIMIT 1),'g1','Admin');
INSERT INTO VIPEngines (name,endpoint,status) VALUES ('e1','http://localhost:5000','enabled');
INSERT INTO VIPResources (name,status,type,configuration) VALUES ('r1',1,'LOCAL','local');
INSERT INTO VIPResourcesEngines (resourcename,enginename) VALUES ('r1','e1');
