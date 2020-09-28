package com.majordemo.image_captioner;

import com.majordemo.autogen.caption.api.ModelApi;
import com.majordemo.autogen.caption.invoker.ApiClient;
import com.majordemo.autogen.caption.model.ModelPredictResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javax.imageio.ImageIO;

@SpringBootApplication
@Configuration
@RestController
public class ImageCaptionerApplication extends WebMvcConfigurationSupport {
	static final String RESPONSE_EVENT_TYPE = "com.majordemo.captioned-image";
	static final String RESPONSE_EVENT_SOURCE = "max-image-captioner";

	@Autowired
	private ModelApi modelApi;

	public static void main(final String[] args) {
		SpringApplication.run(ImageCaptionerApplication.class, args);
	}

	@Override
	public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
		final var imageConverter = new ByteArrayHttpMessageConverter();
		imageConverter.setSupportedMediaTypes(List.of(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG));
		converters.add(imageConverter);
	}

	@Bean
	public ModelApi modelApi(@Value("${CAPTION_SERVICE:http://localhost:5000}") final String serviceHost) {
		System.out.println("Using caption service at " + serviceHost);
		return new ModelApi(apiClient(serviceHost));
	}

	@Bean
	public ApiClient apiClient(final String serviceHost) {
		/*
		 * String serviceHost = System.getenv("CAPTION_SERVICE"); if (serviceHost ==
		 * null || serviceHost == "") { serviceHost = "http://localhost:5000/"; //
		 * Default for development }
		 */
		return new ApiClient().setBasePath(serviceHost);
	}

	@PostMapping(value = "/", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> captionImage(@RequestHeader("ce-id") final String eventId,
			@RequestBody final byte[] imageBytes) throws IOException {
		// We'd love to return ResponseEntity<BufferedImage>, but I can't figure out how
		// to get the response to work.
		BufferedImage image = null;
		image = ImageIO.read(new ByteArrayInputStream(imageBytes));

		String text = "Captioning failed";
		final File toUpload = File.createTempFile("precaption", ".jpg");
		try {
			ImageIO.write(image, "jpg", toUpload);

			System.out.println("Sending " + toUpload.getPath());

			final ModelPredictResponse captions = modelApi.predict(toUpload);
			text = captions.getPredictions().get(0).getCaption();
		} finally {
			Files.delete(toUpload.toPath());
		}

		final Graphics g = image.getGraphics();

		final Font baseFont = new Font("Arial", Font.BOLD, 10);
		final int tenPtWidth = g.getFontMetrics(baseFont).stringWidth(text);

		final int fontHeight = 10 * image.getWidth() / tenPtWidth - 1;

		final Font font = new Font("Arial", Font.BOLD, fontHeight);
		g.setFont(font);
		g.setColor(Color.WHITE);

		final var fm = g.getFontMetrics();
		final int positionX = (image.getWidth() - fm.stringWidth(text)) / 2;
		final int positionY = image.getHeight() - fm.getHeight() / 2; // Put text half a line up from bottom.

		g.drawString(text, positionX, positionY);

		g.drawImage(image, 0, 0, null);
		final var bao = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", bao);

		// Fill out CloudEvents binary headers for response.
		final var responseHeaders = new HttpHeaders();
		responseHeaders.set("ce-type", RESPONSE_EVENT_TYPE);
		responseHeaders.set("ce-source", RESPONSE_EVENT_SOURCE);
		responseHeaders.set("ce-id", eventId);
		responseHeaders.set("ce-specversion", "1.0");

		return ResponseEntity.ok().headers(responseHeaders).contentType(MediaType.IMAGE_JPEG).body(bao.toByteArray());
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") final String name) {
		return String.format("Hello %s!", name);
	}
}
