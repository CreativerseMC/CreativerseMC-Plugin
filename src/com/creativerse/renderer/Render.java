package com.creativerse.renderer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.jmc.Options;

public class Render {

	public File objToPng(String name) {
		InputStream inputStream;
		try {
			// Extract Python script from jar
			String filePath = Bukkit.getServer().getPluginManager().getPlugin("Creativerse").getDataFolder().getAbsoluteFile().getParentFile().getParentFile() + "/cache/render_obj.py";
			File file = new File(filePath);
		    InputStream link;
			link = (getClass().getResourceAsStream("/render_obj.py"));
		    Files.copy(link, file.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
			
			// Render with Blender
		    String outputPath = new File(Bukkit.getServer().getPluginManager().getPlugin("Creativerse").getDataFolder().getAbsoluteFile().getParentFile().getParentFile() + "/cache/" + name + ".png").getAbsolutePath();
			String cmd = "blender --background --python cache/render_obj.py -- cache/" + name + ".obj " + outputPath;
			Runtime run = Runtime.getRuntime();
			Process pr = run.exec(cmd);
			pr.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				System.out.println(line);
			}
			
			// Change padding here
//			addPadding(outputPath, 20);
			return new File(outputPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	// Was going to add padding to the image and then decided against it, but leaving this function here just in case idk
	public void addPadding(String imgPath, int padding) {
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File(imgPath));
		    
		    // Reduce image size to add padding to the top and the bottom and cuts off edges
		    int targetHeight = img.getHeight();
		    int targetWidth = img.getWidth();
		    int resizeHeight = targetHeight - 2*padding;
		    float aspectRatio = (float)targetWidth / (float)targetHeight;
		    int resizeWidth = (int) (aspectRatio * resizeHeight);
		    BufferedImage newImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_4BYTE_ABGR);
		    Graphics2D graphics2D = newImage.createGraphics();
		    
		    // Draw smaller image on a new blank image to create padding
		    graphics2D.drawImage(img, (targetWidth-resizeWidth)/2, padding, resizeWidth, resizeHeight, null);
		    graphics2D.dispose();
	            if (ImageIO.write(newImage, "png", new File(imgPath)))
	            {
	                System.out.println("Added padding to image");
	            }
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
