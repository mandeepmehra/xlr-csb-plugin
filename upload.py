#import sqlite3
import csv
import httplib
import sys
import json
import urllib2

FILE_PATH="/app/sw/xl-release-7.0.0-server/log"
FILE_NAME = "csb-export-release.log"
PATH_SEP = "/"
CSB_URL="https://xebialabs.customersuccessbox.com/api/v1_1/feature"
CSB_API_TOKEN='UZIQodYj/0czMcr4QdnHdiWI0o2xiKvHLqBGdM3shg8='

reader = csv.reader(open(FILE_PATH + PATH_SEP + FILE_NAME), delimiter=',')

#dbConn = sqlite3.connect('data/1.db')
#cursor = dbConn.cursor()
#cursor.execute('''CREATE TABLE data
#             (account text, user text, product text, module text, feature text, timestamp text, uploaded text)''')

for row in reader :
  account   = row[0]
  user      = row[1]
  product   = row[2]
  module    = row[3]
  feature   = row[4]
  timestamp = row[5]	
  
  #insertSql = "insert into data values('%s','%s','%s','%s','%s','%s','N')" % (account, user, product, module, feature, timestamp)
  #print insertSql
  #cursor.execute(insertSql)
  #continue
 
  data = {"account_id" : account,"user_id": user, "product_id":product, "module_id": module, "feature_id": feature, "timestamp" : timestamp }
  print "Sending data .." + str(data)
  req = urllib2.Request(CSB_URL)
  req.add_header('Content-type','application/json')
  req.add_header('Authorization', 'Bearer ' + CSB_API_TOKEN)
  res = urllib2.urlopen(req,json.dumps(data))
  responseJson = json.loads(res.read())
  if responseJson["success"] == True :
     print "Uploaded successfully"

