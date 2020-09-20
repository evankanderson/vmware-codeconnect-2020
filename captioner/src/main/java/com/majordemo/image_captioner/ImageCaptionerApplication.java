package com.majordemo.image_captioner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

@SpringBootApplication
@Configuration
@RestController
public class ImageCaptionerApplication extends WebMvcConfigurationSupport {

	public static void main(String[] args) {
		SpringApplication.run(ImageCaptionerApplication.class, args);
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		var imageConverter = new ByteArrayHttpMessageConverter();
		imageConverter.setSupportedMediaTypes(List.of(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG));
		converters.add(imageConverter);
	}

	@PostMapping(value="/", produces=MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody byte[] captionImage(@RequestBody byte[] imageBytes) throws IOException{
	// We'd love to return ResponseEntity<BufferedImage>, but I can't figure out how to get the response to work.
		BufferedImage image = null;
		image = ImageIO.read(new ByteArrayInputStream(imageBytes));
		Graphics g = image.getGraphics();
		var text = "TEST";

		Font font = new Font("Arial", Font.BOLD, 18);
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
		return ret;
		/*
		 * byte[] response = "Okay".getBytes(); return
		 * ResponseEntity.ok().body(response);
		 */
		// return ResponseEntity.ok().body(image);
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}
}
