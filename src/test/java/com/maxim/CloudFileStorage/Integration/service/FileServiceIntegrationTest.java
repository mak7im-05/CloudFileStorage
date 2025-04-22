package com.maxim.CloudFileStorage.Integration.service;

import com.maxim.CloudFileStorage.Integration.IntegrationTestBase;
import com.maxim.CloudFileStorage.Integration.annotation.IT;
import com.maxim.CloudFileStorage.dto.MinioObjectDto;
import com.maxim.CloudFileStorage.entity.Person;
import com.maxim.CloudFileStorage.security.PersonDetails;
import com.maxim.CloudFileStorage.service.FileService;
import com.maxim.CloudFileStorage.service.MinioService;
import com.maxim.CloudFileStorage.util.PathUtil;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@IT
@AutoConfigureMockMvc
@RequiredArgsConstructor
public class FileServiceIntegrationTest extends IntegrationTestBase {
    private static final String MINIO_USERNAME = "minio";
    private static final String MINIO_PASSWORD = "minio123";
    private static final String BUCKET_NAME = "test-bucket";
    private static final String FIRST_FILE_NAME = "test-file-1.txt";
    private static final String SECOND_FILE_NAME = "test-file-2.txt";
    public static final String REDIRECT_URL = "/main?currentDirectory=";
    public static final String UPLOAD_ENDPOINT = "/upload/files";
    public static final String CURRENT_DIRECTORY = "currentDirectory";
    public static final String RENAME_ENDPOINT = "/file/rename";
    public static final String OLD_NAME = "oldName";
    public static final String NEW_NAME = "newName";
    public static final String NEW_FILE_NAME = "new";
    public static final String DELETE_ENDPOINT = "/file/delete";
    public static final String PATH_TO_FILE = "pathToFile";
    public static final String SEARCH_ENDPOINT = "/search";
    public static final String QUERY = "query";
    MockMultipartFile MOCK_FILE_1 = new MockMultipartFile("files", FIRST_FILE_NAME, "text/plain", "Содержимое файла".getBytes());
    MockMultipartFile MOCK_FILE_2 = new MockMultipartFile("files", SECOND_FILE_NAME, "text/plain", "Содержимое файла".getBytes());
    private static final String PATH = "";
    private static final int FIRST_USER_ID = 1;
    private static final int SECOND_USER_ID = 2;

    private final FileService fileService;
    private final MockMvc mockMvc;
    private final MinioClient minioClient;

    @Container
    static MinIOContainer minioContainer = new MinIOContainer("minio/minio:latest")
            .withUserName(MINIO_USERNAME)
            .withPassword(MINIO_PASSWORD);

    private final MinioService minioService;

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("minio.url", minioContainer::getS3URL);
        dynamicPropertyRegistry.add("minio.accessKey", minioContainer::getUserName);
        dynamicPropertyRegistry.add("minio.secretKey", minioContainer::getPassword);
    }

    @BeforeEach
    void configureMinio() throws Exception {
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(BUCKET_NAME)
                .build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(BUCKET_NAME)
                    .build());
        }
        fileService.createUserFolder(FIRST_USER_ID);
        fileService.createUserFolder(SECOND_USER_ID);
    }

    @AfterEach
    void clear() {
        if(minioService.fileExists(PathUtil.buildFilePath(FIRST_USER_ID, FIRST_FILE_NAME))) {
            fileService.deleteFile(FIRST_USER_ID, FIRST_FILE_NAME);
        }
        if(minioService.fileExists(PathUtil.buildFilePath(FIRST_USER_ID, SECOND_FILE_NAME))) {
            fileService.deleteFile(FIRST_USER_ID, SECOND_FILE_NAME);
        }
        if(minioService.fileExists(PathUtil.buildFilePath(SECOND_USER_ID, FIRST_FILE_NAME))) {
            fileService.deleteFile(SECOND_USER_ID, FIRST_FILE_NAME);
        }
    }

    @Test
    void uploadFolder_shouldRedirectToMain() throws Exception {
        uploadFileTest(MOCK_FILE_1, FIRST_USER_ID);
    }

    @Test
    void renameFile_shouldReturnRedirect_andNewNameAppears() throws Exception {
        uploadFileTest(MOCK_FILE_1, FIRST_USER_ID);
        renameFileTest();
        List<MinioObjectDto> files = fileService.listFiles(PATH, FIRST_USER_ID);
        Assertions.assertEquals(NEW_FILE_NAME + ".txt", files.get(0).name());
    }

    @Test
    void deleteFile_shouldDeleteFile_andReturnRedirect() throws Exception {
        uploadFileTest(MOCK_FILE_1, FIRST_USER_ID);
        deleteFileTest();
        List<MinioObjectDto> files = fileService.listFiles(PATH, FIRST_USER_ID);
        Assertions.assertEquals(1, files.size());
    }

    @Test
    void search_returnsOnlyOwnFiles() throws Exception {
        uploadFileTest(MOCK_FILE_1, FIRST_USER_ID);
        uploadFileTest(MOCK_FILE_2, FIRST_USER_ID);
        uploadFileTest(MOCK_FILE_1, SECOND_USER_ID);

        mockMvc.perform(get(SEARCH_ENDPOINT)
                        .with(authentication(authUser(FIRST_USER_ID)))
                        .param(QUERY,FIRST_FILE_NAME)
                )
                .andExpect(content().string( containsString(FIRST_FILE_NAME) ))
                .andExpect(content().string( not( containsString(SECOND_FILE_NAME) )))
                .andExpect(content().string( not( containsString("\"userId\":2") )));
    }

    private UsernamePasswordAuthenticationToken authUser(int userId) {
        PersonDetails pd = new PersonDetails(new Person(userId, "user" + userId, "pwd"));
        return new UsernamePasswordAuthenticationToken(pd, null, pd.getAuthorities());
    }

    private void uploadFileTest(MockMultipartFile file, int userId) throws Exception {
        mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                        .file(file)
                        .param(CURRENT_DIRECTORY, PATH)
                        .with(authentication(authUser(userId))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(REDIRECT_URL));
    }

    private void renameFileTest() throws Exception {
        mockMvc.perform(post(RENAME_ENDPOINT)
                        .with(csrf()).with(authentication(authUser(1)))
                        .param(CURRENT_DIRECTORY, PATH)
                        .param(OLD_NAME,FIRST_FILE_NAME)
                        .param(NEW_NAME, NEW_FILE_NAME))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(REDIRECT_URL));
    }

    private void deleteFileTest() throws Exception {
        mockMvc.perform(post(DELETE_ENDPOINT)
                        .param(CURRENT_DIRECTORY, PATH)
                        .param(PATH_TO_FILE,FIRST_FILE_NAME)
                        .with(authentication(authUser(FIRST_USER_ID))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(REDIRECT_URL));
    }
}