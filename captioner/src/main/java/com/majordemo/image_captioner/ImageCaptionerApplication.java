package com.majordemo.image_captioner;

import com.majordemo.autogen.caption.api.ModelApi;
import com.majordemo.autogen.caption.invoker.ApiClient;
import com.majordemo.autogen.caption.model.ModelPredictResponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	@Autowired
	private ModelApi modelApi;

	public static void main(String[] args) {
		SpringApplication.run(ImageCaptionerApplication.class, args);
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		var imageConverter = new ByteArrayHttpMessageConverter();
		imageConverter.setSupportedMediaTypes(List.of(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG));
		converters.add(imageConverter);
	}

	@Bean
	public ModelApi modelApi() {
		return new ModelApi(apiClient());
	}

	@Bean
	public ApiClient apiClient() {
		String serviceHost = System.getenv("CAPTION_SERVICE");
		if (serviceHost == null || serviceHost == "") {
			serviceHost = "http://localhost:5000/"; // Default for development
		}
		return new ApiClient().setBasePath(serviceHost);
	}

	@PostMapping(value = "/", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> captionImage(@RequestBody byte[] imageBytes) throws IOException {
		// We'd love to return ResponseEntity<BufferedImage>, but I can't figure out how
		// to get the response to work.
		BufferedImage image = null;
		image = ImageIO.read(new ByteArrayInputStream(imageBytes));

		String text = "Captioning failed";
		File toUpload = File.createTempFile("precaption", ".jpg");
		try {
			ImageIO.write(image, "jpg", toUpload);

			System.out.println("Sending " + toUpload.getPath());

			ModelPredictResponse captions = modelApi.predict(toUpload);
			text = captions.getPredictions().get(0).getCaption();
		} finally {
			Files.delete(toUpload.toPath());
		}

		Graphics g = image.getGraphics();

		Font baseFont = new Font("Arial", Font.BOLD, 10);
		int tenPtWidth = g.getFontMetrics(baseFont).stringWidth(text);

		int fontHeight = 10 * image.getWidth() / tenPtWidth - 1;

		Font font = new Font("Arial", Font.BOLD, fontHeight);
		g.setFont(font);
		g.setColor(Color.WHITE);

		var fm = g.getFontMetrics();
		int positionX = (image.getWidth() - fm.stringWidth(text)) / 2;
		int positionY = image.getHeight() - fm.getHeight() / 2; // Put text half a line up from bottom.

		g.drawString(text, positionX, positionY);

		g.drawImage(image, 0, 0, null);
		var bao = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", bao);
		var ret = bao.toByteArray();

		System.out.println("Writing image of " + ret.length + " bytes\n\n");
		return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}
}
