# Code for CODE4212: Serverless on your own terms with Knative

The talk: https://vmwarecodeconnect.github.io/CodeConnect2020/Evan/

The components:

- Image-caption-generator is based on the
  [IBM MAX Image Caption Generator](https://github.com/IBM/MAX-Image-Caption-Generator). It is built unmodified from 
- Captioner is a Spring Boot Java application which reads an image from a POST
  body, calls the image-caption-generator to determine a caption, and then
  composites the caption onto the image and returns a new image.
