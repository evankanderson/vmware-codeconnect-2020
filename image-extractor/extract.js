const express = require("express");
const { Receiver, CloudEvent, HTTP } = require("cloudevents");
const bodyParser = require("body-parser");
const fetch = require("node-fetch");
const { response } = require("express");

const app = express();
app.use(bodyParser.json());
app.use(bodyParser.raw({limit: '10mb', type: ['image/jpeg', 'image/png', 'image/jpg']}))

const sourceUrl = "http://" + process.env.K_SERVICE + "/";
const responseEventType = "com.majordemo.twitter-image";
const port = process.env.PORT || 3000;

app.get("/", (req, res) => {
  res.send(
    "This is an event transcoder, try POSTing tweets in CloudEvents format instead."
  );
});

app.post("/", (req, res) => {
  const event = HTTP.toEvent({headers: req.headers, body: req.body});

  if (!(event.data.entities && event.data.entities.media)) {
    console.log("Skipping tweet %s, it has no media", event.id);
    return;
  }

  const mediaUrl = event.data.entities.media[0].media_url;

  console.log("Fetching '%s' for tweet %s", mediaUrl, event.id);

  fetch(mediaUrl).then(async (resp) => {
    let newEvent = event.cloneWith({
      source: sourceUrl,
      type: responseEventType,
      datacontenttype: resp.headers.get("Content-Type")
    })
    // It turns out that response.body will be (incorrectly) base64-encoded here. Just send the original blob.
    let payload = await resp.arrayBuffer();
    const response = HTTP.binary(newEvent);
    console.log('Blob length was: ' + payload.byteLength)

    res.status(200).set(response.headers).send(Buffer.from(payload));
  });
});

console.log("Starting on port " + port + "...");

app.listen(port);
