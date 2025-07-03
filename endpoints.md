# Media Gallery endpoints
1. `POST /api/media/upload`  
Used for uploading media files. Requires a file to save in bucket and either parentId or kidId (If not provided or provided both it would return an error)  
```
curl -X POST http://localhost:8080/api/media/upload \
  -F "file=@/destination/file.jpg" \
  -F "parentId=1"
```
2. `GET /api/media/{id}`  
Returns media of specified id.
```
curl -X GET http://localhost:8080/api/media/1
```
3. `GET /api/media/{id}/download`  
Allows to download specified media to host machine.
```
curl -X GET http://localhost:8080/api/media/1/download
```
4. `DELETE /api/media/{id}`  
Allows to delete specified media.
```
curl -X DELETE http://localhost:8080/api/media/1
```

# Auth endpoints
1. `POST /auth/signup`
Used for registration of new Parent. Requires specified JSON structure.
```
{
    "email": "xyz@xyz.xyz",
    "password": "xyz"
}
```
```
curl -X POST http://localhost:8080/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "email": "user@example.com",
    "password": "tajneHaslo123"
}'
```
2. `POST /auth/login`  
Used for authentication of a Parent. Requires same JSON as signup endpoint and same API call. Returns token and expiration time.
```
{
    "token": "eyJhbGciOiJIUzI1NiJ9....",
    "expiresIn": 3600000
}
```

# Kids endpoints
1. `POST /api/kids/new`  
Creates new Kid and assign it to its parent. Requires specified JSON structure.
```
{
    "name": "xyz",
    "birthDate": "2025-01-01"
}
```
```
curl -X POST http://localhost:8080/api/kids/new \
-H "Content-Type: application/json" \
-d '{
    "name": "xyz",
    "birthDate": "2025-01-01"
}'
```
2. `DELETE /api/kids/{id}`
Deletes kid of specified id
```
curl -X DELETE http://localhost:8080/api/kids/1
```
3. `GET /api/kids/{id}`  
Returns information of kid of specified id
```
curl -X DELETE http://localhost:8080/api/kids/1
```
4. `GET /api/kids/{id}`  
   Returns information of kid of specified id
```
curl -X DELETE http://localhost:8080/api/kids/1
```

# Tasks endpoints
1. `POST /api/tasks`
Creates new task, we could either pass kidIds, or not but in our implementation kids will be assigned to task at its creation
```
{
  "title": "XYZ",
  "description": "XYZ",
  "taskStart": "2025-07-01T01:00:00",
  "taskEnd": "2025-07-04T10:00:00",
  "status": "PENDING",
  "note": "XYZ",
  "parentId": 1,
  "kidIds": [1, 2],
  "isSynced": "false"
}
```
```
curl -X POST http://localhost:8080/api/tasks \
-H "Content-Type: application/json" \
-d '{
    "title": "XYZ",
    "description": "XYZ",
    "taskStart": "2025-07-01T01:00:00",
    "taskEnd": "2025-07-04T10:00:00",
    "status": "PENDING",
    "note": "XYZ",
    "parentId": 1,
    "kidIds": [1, 2],
    "isSynced": "false"
}'
```
2. `GET /api/tasks/{taskId}`  
Returns task of specified taskId
```
curl -X GET http://localhost:8080/api/tasks/1
```
3. `GET /api/tasks/parent/{parentId}`  
Returns tasks of parent of specified parentId
```
curl -X GET http://localhost:8080/api/tasks/parent/1
```
4. `GET /api/tasks/kid/{kidId}`  
Returns task of kid of specified kidId
```
curl -X GET http://localhost:8080/api/tasks/kid/1
```
5. `PUT /api/tasks/{taskId}`  
Update information of task specified by taskId, requires same JSON and request structure as endpoint 1.
6. `DELETE /api/tasks/{taskId}`  
Remove task of specified taskId
7. `POST /api/tasks/{taskId}/kids/{kidId}`  
Adds kid of kidId to the specific task of taskId
8. `DELETE /api/tasks/{taskId}/kids/{kidId}`
Removes kid of kidId from the specific task of the taskId