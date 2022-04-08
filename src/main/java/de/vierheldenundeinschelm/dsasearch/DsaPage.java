package de.vierheldenundeinschelm.dsasearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DsaPage {

    private String pdfPath;
    private int number;
    private String text;

}