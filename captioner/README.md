## Packaging:

```shell
docker build . -t ekanderson/java-captioner
docker push ekanderson/java-captioner
```

## Deploy:

```shell
kn service create captioner --image ekanderson/java-captioner --env CAPTION_SERVICE=http://max-image-caption-generator:5000/
```