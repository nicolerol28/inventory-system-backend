package com.miapp.inventory_system.shared.gateway;

public interface StorageGateway {

    /**
     * Uploads a file to the storage provider and returns its public URL.
     *
     * @param fileName    the target file name (including path/key if needed)
     * @param fileContent the raw bytes of the file
     * @param contentType the MIME type of the file (e.g. "image/png")
     * @return the public URL of the uploaded file
     * @throws RuntimeException if the upload fails
     */
    String uploadFile(String fileName, byte[] fileContent, String contentType);
}
