package de.vierheldenundeinschelm.dsasearch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DsaSearchController {

    private final SearchEngine searchEngine;

    private final DsaPdfCrawler dsaPdfCrawler;

    @GetMapping("/dsasuche")
    public String dsaSuche(@RequestParam Optional<String> search, Model model) throws Exception {
        if(search.isPresent()) {
            List<DsaPage> result = searchEngine.search(search.get());
            var gruppiert = result.stream()
                .sorted((o1, o2) -> o1.getNumber()-o2.getNumber())
                .map(page -> verkuerze(page, search.get()))
                .collect(Collectors.groupingBy(page -> page.getPdfPath()));
            model.addAttribute("result", gruppiert);
        }
        return "suche";
    }

    private DsaPage verkuerze(DsaPage page, String s) {
        int start = page.getText().toLowerCase().indexOf(s.toLowerCase());
        int end = start+s.length();
        start = Math.max(0, start-100);
        end = Math.min(page.getText().length(), end+100);
        page.setText(page.getText().substring(start, end));
        return page;
    }

    @GetMapping("/dsasuche/crawl")
    public String crawl() throws Exception {
        dsaPdfCrawler.crawl();
        return "suche";
    }

}
