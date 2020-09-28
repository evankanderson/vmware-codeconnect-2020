This is based on
https://github.com/GoogleCloudPlatform/buildpack-samples/tree/master/sample-node

Converts a Twitter CloudEvent from https://github.com/vaikas/twitter event
source by extracting the image and preserving the event metadata, then returns a
new event with the event payload being the bytes of the `media_url` extracted
from the tweet.

## To build:

```shell
pack build --builder gcr.io/buildpacks/builder:v1 ekanderson/image-extractor
docker push ekanderson/image-extractor
```

## To run:

```
kn service create tweet-extractor --image ekanderson/image-extractor
```