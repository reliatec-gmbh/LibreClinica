package org.akaza.openclinica.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

public class DebugHelper implements Serializable {
    public DebugHelper() {
        super();
    }

    public String getCurrentGitBranch() {
        // Obtained from https://stackoverflow.com/questions/49106104/get-current-git-branch-inside-a-java-test
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD");
            process.waitFor();
        } catch (IOException e) {
            try {
                process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "git rev-parse --abbrev-ref HEAD"});
                process.waitFor();
            } catch (IOException ex2) {
                ex2.printStackTrace();
                return "Unknown(error)";
            } catch (InterruptedException ex3) {
                ex3.printStackTrace();
                return "Unknown(interrupted)";
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);

        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "Unknown(error reading command output)";
        }
    }
}
