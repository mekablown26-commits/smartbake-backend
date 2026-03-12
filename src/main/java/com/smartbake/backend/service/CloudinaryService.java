package com.smartbake.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) throws IOException {
       Map<String, Object> uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "folder", "smartbake/products",
                "resource_type", "image"
            )
        );
        return (String) uploadResult.get("secure_url");
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        try {
            // Extract public_id from URL
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
        }
    }

    private String extractPublicId(String url) {
        // URL format: https://res.cloudinary.com/cloud/image/upload/v123/smartbake/products/filename.jpg
        try {
            String[] parts = url.split("/upload/");
            if (parts.length < 2) return null;
            String withVersion = parts[1];
            // Remove version if present (v1234567/)
            String publicIdWithExt = withVersion.replaceFirst("v\\d+/", "");
            // Remove extension
            int dotIndex = publicIdWithExt.lastIndexOf('.');
            return dotIndex > 0 ? publicIdWithExt.substring(0, dotIndex) : publicIdWithExt;
        } catch (Exception e) {
            return null;
        }
    }
}
