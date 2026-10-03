package com.telecom.docs;

import com.telecom.service.CustomerService;
import com.telecom.service.PlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.server.PathContainer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs in `verify` (Failsafe), after the documentation tests generated their snippets:
 * every /api endpoint must have at least one documented example request.
 */
@WebMvcTest
class ApiDocumentationCoverageIT {

    private static final Path SNIPPETS = Path.of("target", "generated-snippets");
    private static final Pattern REQUEST_LINE = Pattern.compile("^(GET|POST|PUT|PATCH|DELETE) (\\S+) HTTP/1\\.1$");

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private PlanService planService;

    @Test
    void everyApiEndpointHasAGeneratedExample() throws IOException {
        assertThat(SNIPPETS).as("Run the documentation tests first (mvn verify)").isDirectory();
        List<String[]> documented = documentedRequests();

        List<String> undocumented = new ArrayList<>();
        mappings.getHandlerMethods().keySet().forEach(info -> {
            for (String pattern : info.getPatternValues()) {
                if (!pattern.startsWith("/api/")) {
                    continue;
                }
                PathPattern path = PathPatternParser.defaultInstance.parse(pattern);
                for (RequestMethod method : info.getMethodsCondition().getMethods()) {
                    boolean covered = documented.stream().anyMatch(r ->
                            r[0].equals(method.name()) && path.matches(PathContainer.parsePath(r[1])));
                    if (!covered) {
                        undocumented.add(method + " " + pattern);
                    }
                }
            }
        });
        assertThat(undocumented).as("Endpoints without REST Docs").isEmpty();
    }

    private static List<String[]> documentedRequests() throws IOException {
        try (Stream<Path> files = Files.walk(SNIPPETS)) {
            return files.filter(p -> p.getFileName().toString().equals("http-request.adoc"))
                    .flatMap(p -> {
                        try {
                            return Files.readAllLines(p).stream();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .map(REQUEST_LINE::matcher)
                    .filter(Matcher::matches)
                    .map(m -> new String[]{m.group(1), m.group(2).split("\\?")[0]})
                    .toList();
        }
    }
}
