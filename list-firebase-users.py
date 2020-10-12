import os

import firebase_admin
import firebase_admin.auth as firebase_auth

print(os.getenv('GOOGLE_APPLICATION_CREDENTIALS'))

default_app = firebase_admin.initialize_app()

usersResponse = firebase_auth.list_users(app=default_app)
print("Fetched " + str(len(usersResponse.users)) + " users stored by FireBase")
print()

for userRecord in usersResponse.users:
  if (len(userRecord.provider_data) > 0):
    for providerInfo in userRecord.provider_data:
      if(str(providerInfo.provider_id) == 'apple.com'
        and userRecord.email != None ):
        print()
        print("User: " + str(userRecord.display_name))
        print("User Email: " + str(userRecord.email))
        print("PROVIDER: " + str(providerInfo.provider_id))
        print("UID: " + str(providerInfo.uid))
