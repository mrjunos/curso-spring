package com.mrjunos.models.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadFileService implements IUploadFileService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final String UPLOADS_FOLDER = "uploads";

	@Override
	public Resource load(String fielName) throws MalformedURLException {
		Path pathFile = getPath(fielName);
		log.info("pathFile: " + pathFile);
		Resource recurso = null;

		recurso = new UrlResource(pathFile.toUri());
		if (!recurso.exists() || !recurso.isReadable()) {
			throw new RuntimeException("Error: no se puede cargar la imagen: " + pathFile.toString());
		}

		return recurso;
	}

	@Override
	public String copy(MultipartFile file, String fileId) throws IOException {
		String fileName = fileId + "_" + file.getOriginalFilename();
		Path rootPath = getPath(fileName);
		Files.copy(file.getInputStream(), rootPath);
		return fileName;
	}

	@Override
	public boolean delete(String fileName) {
		Path rootPath = getPath(fileName);
		File file = rootPath.toFile();

		if (file.exists() && file.canRead()) {
			if (file.delete()) {
				return true;
			}
		}
		return false;
	}

	public Path getPath(String fileName) {
		return Paths.get(UPLOADS_FOLDER).resolve(fileName).toAbsolutePath();
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(Paths.get(UPLOADS_FOLDER).toFile());
	}

	@Override
	public void init() throws IOException {
		Files.createDirectory(Paths.get(UPLOADS_FOLDER));
	}

}
