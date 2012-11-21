package uk.ac.tgac.rampart.service.impl;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Service;

import uk.ac.tgac.rampart.service.LatexService;
import uk.ac.tgac.rampart.util.ProcessStreamManager;

@Service
public class LatexServiceImpl implements LatexService {
	
	@Override
	public void compileDocument(File texFile) throws Exception {
		
		// Assumes latex is installed and pdflatex is on the path
		File workingDir = new File(texFile.getParent());
		String command = "pdflatex -interaction=nonstopmode " + texFile;

		Process process = Runtime.getRuntime().exec(command, new String[]{}, workingDir);
		ProcessStreamManager psm = new ProcessStreamManager(process, "PDFLATEX");

		int code = psm.runInForeground(false);

		if (code != 0) {
			throw new IOException("PDFLATEX returned code " + code);
		}
	}
}