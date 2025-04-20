package xyz.kbws.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.PathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * @author kbws
 * @date 2025/4/20
 * @description:
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class JGitTest {
    @Resource
    private OllamaChatClient ollamaChatClient;
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private SimpleVectorStore simpleVectorStore;
    @Resource
    private PgVectorStore pgVectorStore;

    @Before
    public void setProxy() {
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "7890");
        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyPort", "7890");
    }


    @Test
    public void test() throws GitAPIException, IOException {
        String repoURL = "https://github.com/kbws13/K-CodeReview";

        String localPath = "./cloned-repo";
        log.info("克隆路径：{}", new File(localPath).getAbsolutePath());

        FileUtils.deleteDirectory(new File(localPath));

        Git git = Git.cloneRepository()
                .setURI(repoURL)
                .setDirectory(new File(localPath))
                .call();

        git.close();
    }

    @Test
    public void test_file() throws IOException {
        Files.walkFileTree(Paths.get("./cloned-repo"), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                log.info("文件路径:{}", file.toString());

                PathResource resource = new PathResource(file);
                TikaDocumentReader reader = new TikaDocumentReader(resource);

                List<Document> documents = reader.get();
                List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

                documents.forEach(doc -> doc.getMetadata().put("knowledge", "K-CodeReview"));
                documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "K-CodeReview"));

                pgVectorStore.accept(documentSplitterList);

                return FileVisitResult.CONTINUE;
            }
        });
    }
}
