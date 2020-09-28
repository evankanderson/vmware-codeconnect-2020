# Code for CODE4212: Serverless on your own terms with Knative

The talk: https://vmwarecodeconnect.github.io/CodeConnect2020/Evan/

The components:

- Image-caption-generator is based on the
  [IBM MAX Image Caption Generator](https://github.com/IBM/MAX-Image-Caption-Generator).
  It is built, unmodified, into a Docker container from the IBM project.

- Captioner is a Spring Boot Java application which reads an image from a POST
  body, calls the image-caption-generator to determine a caption, and then
  composites the caption onto the image and returns a new image.

- Image-Extractor is a Node.js application which accepts a `com.twitter.tweet`
  CloudEvent from the https://github.com/vaikas/twitter event source, finds the
  first image in the tweet, fetches it, and returns a new event
  (`com.majordemo.twitter-image`) containing the image as binary data.

- configs contains additional Knative object definitions (in particular, trigger
  definitions) to support the entire workflow.


## Useful testing commands:

- Captioner:
  ```curl -v -H "Content-Type: image/jpg" --data-binary "@./some-image-you-found.jpg" $TARGET -o captioned.jpg```

- Image-Extractor:
  ```curl -vv -H 'ce-id: 1310437820780216321' -H 'ce-source: https://twitter.com/' -H 'ce-type: com.twitter.tweet' -H 'ce-specversion: 1.0' -H 'content-type: application/json' -d '@.\payload.json' $TARGET -o .\temp.jpg```