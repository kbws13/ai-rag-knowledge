package xyz.kbws.api;

import org.springframework.web.multipart.MultipartFile;
import xyz.kbws.api.response.Response;

import java.util.List;

/**
 * @author kbws
 * @date 2025/4/20
 * @description:
 */
public interface IRAGService {

    Response<List<String>> queryRagTagList();

    Response<String> uploadFile(String ragTag, List<MultipartFile> files);

    Response<String> analyzeGitRepository(String repoUrl, String userName, String token) throws Exception;
}
