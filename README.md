# AWS Alexa Music Man skill

This Java based AWS Alexa SDK Skill is an Amazon Alexa which incorporates DynamoDB, SNS, and external API data to provide a highly
dynamic customer experience for obtaining Artist and Venue calendar information. 
This skill also makes use of Amazon's APL (**Alexa Presentation Language**) to provide a better visual output 
when invoked from display based consumer devices (e.g. Echo Show, Spot, and Smart TVs)

- **AWS DynamoDB** is used to house a lookup table, which the code uses to bounce up captured voice input (artist and venue values) and swap them out for 
corrected values. 
- **AWS SNS** is used to post to a pre-configured topic, setup to deliver a message to an email subscriber. For this skill, 
the purpose for this is to send customer invocations that failed, so they can be researched and corrected.
- **Songkick API** is an external API service for getting Artist and Venue event information. This code calls the API using the input captured
from the user, to try to find matching events for artists or venues. 

Link:  http://www.songkick.com/
http://www.songkick.com/developer

![Image of Songkick ](http://static.tumblr.com/yfms2o4/HKnl73h3y/logo_for_tumblr_fullname.png) 

Attribution to **Vishnu R Nair** for the free background image from **Unsplash**

Link: https://unsplash.com/photos/m1WZS5ye404


### Required for the projects to run
A **config.properties** is needed to house a few key values needed by the Skill so as to not
 be hard coded inside the app.  This file should be placed in the **src/main/java/resources** directory

config.properties should have the following entries:
```
#AWS Settings
SkillID=<The AWS skill id defined in the AWS Alexa developer console - used to restrict access to this code when hosted in lambda>
DynamoDBTable=<AWS Dynamo DB Table name>
SNSMessageTopic=<AWS SNS arn for topic to push messages to>

#Songkick settings
apikey=<Key for access to Songkick APIs>

#App settings
AppTitle=<title of application, displayed in the Alexa console cards>
LargeImageUrl=<URL of larger 546px .png graphic used for display in Alexa console standard cards>
SmallImageUrl=<URL of smaller 340px .png graphic used for display in Alexa console standard cards>
```

