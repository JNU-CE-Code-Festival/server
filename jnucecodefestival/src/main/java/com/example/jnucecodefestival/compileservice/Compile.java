package com.example.jnucecodefestival.compileservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.io.Writer;

public class Compile {
    private long timeout = 1;
    /**
     * compile
     * 
     * @param lang String ???. ????????? ?????? ??????. Available(c, cpp, node, python)
     * @param createAuthor String ???. 
     * @return
     */
    public static String compile(final String lang, final String createAuthor, final String code, final String number, final String grade) {
        StringBuilder resultStringBuilder = new StringBuilder();
        Compile compile = new Compile();

        // ?????? ?????? ?????? ?????? : compile/?????????14/?????????14-2018-11-10.java
        String filePath = "compile/" + createAuthor + "/" + number + "/" + lang;
        String fileName = createAuthor + "-" + new SimpleDateFormat("yy-MM-dd-kk-mm-ss").format(new Date()) + "." + lang;

        compile.hasFolder(filePath);
        compile.makeFile(filePath, fileName, code);

        // ?????? ???????????? ???????????? ?????????
        if(lang.equals("java")) compile.makeFile(filePath, "Main.java", code);
        
        try {
            switch(lang) {
                case "c":
                    resultStringBuilder = compile.cCompile(filePath, fileName);
                    break;
                case "cpp":
                    resultStringBuilder = compile.cppCompile(filePath, fileName);
                    break;
                case "js":
                    resultStringBuilder = compile.runNode(filePath, fileName);
                    break;
                case "py":
                    resultStringBuilder = compile.runPython(filePath, fileName);
                    break;
                case "java":
                    resultStringBuilder = compile.javaCompile(filePath, "Main.java");
                    break;
                default:
                    resultStringBuilder.append("??????????");
            }
        } catch(RuntimeException ioe) { 
            resultStringBuilder.append("????????? ??????????????????.");
        }

        return resultStringBuilder.toString().trim() != "" ? resultStringBuilder.toString() : "?????? ??????!";
    }
    
    public StringBuilder cCompile(final String filePath, final String fileName) {

        String executeFilePath = filePath + "/a.out";
        StringBuilder sb = new StringBuilder();
        Runnable cRun = () -> {
            try {
                final Process compile = 
                new ProcessBuilder()
                    .command("gcc", "-O2", filePath + "/" + fileName, "-o", executeFilePath)
                    .start();
                    
                getOutputStringBuilder(sb, compile, true);
                compile.waitFor();

                if(sb.length() != 0) return;

                final Process execute =
                new ProcessBuilder()
                    .command("bash", "-c", executeFilePath)
                    .start();
                
                getOutputStringBuilder(sb, execute, false);

                if(!execute.waitFor(timeout, TimeUnit.SECONDS)) {
                    execute.destroy();
                    sb.setLength(0);
                    throw new RuntimeException();
                }

                deleteFile(executeFilePath);
            } catch (Exception io) {
                throw new RuntimeException(io);
            }
        };

        startSubProcess(cRun, sb);
        return sb;
    }

    public StringBuilder cppCompile(String filePath, String fileName) {
        String executeFilePath = filePath + "/a.out";
        StringBuilder sb = new StringBuilder();
        Runnable cRun = () -> {
            try {
                final Process compile = 
                new ProcessBuilder()
                    .command("g++", "-std=c++11", "-O2", "-Wno-unused-result", filePath + "/" + fileName, "-o", executeFilePath)
                    .start();

                getOutputStringBuilder(sb, compile, true);
                compile.waitFor();

                if(sb.length() != 0) return;

                final Process execute =
                new ProcessBuilder()
                    .command("bash", "-c", executeFilePath)
                    .start();
                
                getOutputStringBuilder(sb, execute, false);

                if(!execute.waitFor(timeout, TimeUnit.SECONDS)) {
                    execute.destroy();
                    sb.setLength(0);
                    throw new RuntimeException();
                }

                deleteFile(executeFilePath);
            } catch (Exception io) {
                throw new RuntimeException(io);
            }
        };

        startSubProcess(cRun, sb);
        return sb;
    }

    public StringBuilder javaCompile(final String filePath, final String fileName) {
        StringBuilder sb = new StringBuilder();

        Runnable javaRun = () -> {
            try {
                // "-Xstdout", filePath + "/errorCode.txt",
                final Process compile = 
                new ProcessBuilder()
                .command("javac", "-encoding", "UTF-8",  filePath + "/" + fileName)
                .start();

                getOutputStringBuilder(sb, compile, true);
                compile.waitFor();

                if(sb.length() != 0) return;

                final Process execute =
                new ProcessBuilder()
                .command("java", "-cp", filePath, "-Dfile.encoding=utf-8", "Main")
                .start();

                getOutputStringBuilder(sb, execute, false);

                if(!execute.waitFor(timeout, TimeUnit.SECONDS)) {
                    execute.destroy();
                    sb.setLength(0);
                    throw new RuntimeException();
                }

                deleteFile(filePath + "/" + "Main.class");
    
            } catch (Exception io){
                throw new RuntimeException();
            }
        };

        startSubProcess(javaRun, sb);
        return sb;
    }

    public StringBuilder runNode(final String filePath, final String fileName) {
        StringBuilder sb = new StringBuilder();
        
        Runnable jsRun = () -> {
            try {
                final Process execute = 
                new ProcessBuilder()
                .command("node", filePath + "/" + fileName).start();
                getOutputStringBuilder(sb, execute, false);

                if(!execute.waitFor(timeout, TimeUnit.SECONDS)) {
                    execute.destroy();
                    sb.setLength(0);
                    throw new RuntimeException();
                }
            } catch (Exception io) {
                throw new RuntimeException();
            }
        };

        startSubProcess(jsRun, sb);

        return sb;
    }

    public StringBuilder runPython(final String filePath, final String fileName) {
        StringBuilder sb = new StringBuilder();
        
        Runnable pyRun = () -> {
            try {
                final Process execute = 
                new ProcessBuilder()
                .command("python3", filePath + "/" + fileName).start();
                getOutputStringBuilder(sb, execute, false);
                if(!execute.waitFor(timeout, TimeUnit.SECONDS)) {
                    execute.destroy();
                    sb.setLength(0);
                    throw new RuntimeException();
                }
            } catch (Exception io) {
                throw new RuntimeException();
            }
        };

        startSubProcess(pyRun, sb);

        return sb;
    }

    public void hasFolder(final String filePath) {
        File targetDir = new File(filePath);
        if(!targetDir.exists()) targetDir.mkdirs();
    }

    public void makeFile(final String filePath, final String fileName, final String code) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath + "/" + fileName), "UTF-8"));
            writer.write(code);
        } catch (IOException ex) {
            System.out.println("파일 생성중 문제 발생!!");
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {}
        }
    }

    private static void deleteFile(String filePath) {
        File classFile = new File(filePath);
        if(classFile.exists()) {
            if(classFile.delete()) {
                System.out.println("파일삭제완료");
            }
        }
    }

    private static StringBuilder getOutputStringBuilder(StringBuilder sb, Process execute, boolean errorBool) throws IOException {
        InputStream exStream = errorBool ? execute.getErrorStream() : execute.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(exStream));
        String line;
        while((line=br.readLine()) != null) {
            sb.append(line);
            sb.append(System.getProperty("line.separator"));
        }
        br.close();
        return sb;
    }

    private void startSubProcess(Runnable runnable, StringBuilder sb) {
        Thread build = new Thread(runnable);
        build.start();
        try {
            build.join();
        } catch (InterruptedException io) {
            sb.append("런타임 에러!");
        }
    }
}