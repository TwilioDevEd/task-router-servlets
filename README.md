<a href="https://www.twilio.com">
  <img src="https://static0.twilio.com/marketing/bundles/marketing/img/logos/wordmark-red.svg" alt="Twilio" width="250" />
</a>

# TaskRouter on Servlets

[![Build Status](https://travis-ci.org/TwilioDevEd/task-router-servlets.svg?branch=master)](https://travis-ci.org/TwilioDevEd/task-router-servlets)

Use Twilio to provide your user with multiple options through phone calls, so
they can be assisted by an agent specialized in the chosen topic. This is
basically a call center created with the Task Router API of Twilio. This example
uses a SQLite database to log phone calls which were not assisted.

[Read the full tutorial here](//www.twilio.com/docs/tutorials/walkthrough/task-router/java/servlets)

### Prerequisites

1. [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
   installed in your operative system.
1. A Twilio account with a verified [phone number][twilio-phone-number].
   (Get a [free account](//www.twilio.com/try-twilio?utm_campaign=tutorials&utm_medium=readme)
   here.). If you are using a Twilio Trial Account, you can learn all about it [here]
   (https://www.twilio.com/help/faq/twilio-basics/how-does-twilios-free-trial-work).

### Local Development

1. First clone this repository and `cd` into it.
  ```
  $ git clone git@github.com:TwilioDevEd/task-router-servlets.git
  $ cd task-router-servlets
  ```

1. Edit the sample configuration file `.env.example` and edit it to match
 your configuration.

  You can use the `.env.example` in a unix based operative system with the
  `source` command to load the variables into your environment:

  ```bash
  $ source .env.example
  ```

  If you are using a different operating system, make sure that all the
  variables from the `.env.example` file are loaded into your environment.

1. Make sure the tests succeed.

  ```bash
  $ ./gradlew check
  ```

1. Configure the phone number of the agents which are going to answer the calls.

  ```bash
    $ ./gradlew -q createWorkspace -Pconfig="https://<sub-domain>.ngrok.io <agent1-phone> <agent2-phone>"
  ```

  You will receive a message telling you to export 2 environment variables.

  ```bash
  $ export WORKFLOW_SID=<hashvalue-workflow-sid>
  $ export POST_WORK_ACTIVITY_SID=<hashvalue-post-work-activity-sid>
  ```
  When the user calls, he will choose one option which will redirect him to
  the first agent whose phone number is __agent1-phone__. If the user gets
  no answer in 30 seconds he will be redirected to the second agent whose
  phone number is __agent2-phone__.

1. Start the server.

  ```bash
  $ ./gradlew appRun
  ```
1. Expose your local web server to the internet using ngrok.

  You can click [here](https://www.twilio.com/blog/2015/09/6-awesome-reasons-to-use-ngrok-when-testing-webhooks.html)
  for more details. This step is important because the application won't
  work as expected if you run it using `localhost`.

  ```bash
  $ ngrok http 8000
  ```

  Once ngrok is running open up your browser and go to your ngrok URL. It will look something like this:

  `http://<sub-domain>.ngrok.io/`

1. Configure Twilio to call your webhooks.

   You will also need to configure Twilio to call your application via POST when
   phone calls are received on your _Twilio Number_. The configuration of
   **Voice** should look something like this:

   ```
   http://<sub-domain>.ngrok.io/call/incoming
   ```

   ![Configure SMS](http://howtodocs.s3.amazonaws.com/twilio-number-config-all-med.gif)

### How To Demo?

1. Call your Twilio Phone Number. You will get a voice response:

  > For Programmable SMS, press one.
  For Voice, press any other key.

1. Reply with 1.
1. The specified phone for agent 1 will be called:  __agent1-phone__.
1. If __agent1-phone__ is not answered in 30 seconds then __agent2-phone__ will
  be called.
1. In case the second agent doesn't answer the call, it will be logged as a
  missed call. You can see all missed calls in the main page of the running
  server at [http://{sub-domain}.ngrok.io](//localhost:8000).
1. Repeat the process but enter any key different to __1__ to choose Voice.

 [twilio-phone-number]: https://www.twilio.com/console/phone-numbers/incoming

 ## Meta

 * No warranty expressed or implied. Software is as is. Diggity.
 * [MIT License](http://www.opensource.org/licenses/mit-license.html)
 * Lovingly crafted by Twilio Developer Education.
