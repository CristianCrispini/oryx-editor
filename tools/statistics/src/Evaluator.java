import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.DiagramBuilder;
import org.oryxeditor.server.diagram.Shape;
import org.springframework.util.FileCopyUtils;

/**
 * The {@code Evaluator} class processes JSON files representing diagrams.
 * It performs various evaluations on the diagrams, such as counting specific
 * stencil types and generating a CSV output summarizing the results.
 * 
 * This class identifies and analyzes BPMN diagrams based on the stencil set URL,
 * performs validations, and tracks occurrences of specific properties within the diagrams.
 * 
 * The main functionality includes filtering input files, parsing JSON diagrams,
 * collecting statistics, and writing the results to an output file.
 * 
 * @author Philipp
 * @version 1.0
 */
public class Evaluator {

    /**
     * A {@code FilenameFilter} implementation to filter JSON files.
     */
    static class JSONFilter implements FilenameFilter {
        /**
         * Determines if a given file should be accepted based on its name.
         *
         * @param dir  the directory in which the file was found
         * @param name the name of the file
         * @return {@code true} if the file ends with ".json"; {@code false} otherwise
         */
        public boolean accept(File dir, String name) {
            return (name.endsWith(".json"));
        }
    }

    /**
     * Main entry point for the application. Processes JSON files in a given directory,
     * analyzes their content, and outputs a summary to a CSV file.
     *
     * @param args command-line arguments; expects exactly one argument specifying the directory path
     */
    public static void main(String[] args) {
        int count = 0;
        StringBuilder invalid = new StringBuilder();
        if (args.length != 1) {
            System.err.println("Wrong Number of Arguments!");
            System.err.println(usage());
            return;
        }
        String jsonDirPath = args[0];
        File dir = new File(jsonDirPath);
        System.out.println(jsonDirPath);
        File[] files = dir.listFiles(new JSONFilter());
        List<Diagram> diagrams = new ArrayList<>();
        HashSet<String> sets = new HashSet<>();
        HashMap<String, String> lines = new HashMap<>();

        for (File f : files) {
            try {
                String str = FileCopyUtils.copyToString(new FileReader(f));
                str = str.replace("\"target\":{},", "");
                Diagram d = DiagramBuilder.parseJson(str);
                String set = d.getStencilset().getUrl();
                if (!set.contains("bpmn1.1.json") && !set.contains("bpmn.json"))
                    continue;
                HashMap<String, Integer> counter = new HashMap<>();
                countDiagram(d, counter);

                String models = lines.get("");
                int modelCount = (models != null) ? models.split(";").length : 0;
                Set<String> keys = new HashSet<>(counter.keySet());
                keys.removeAll(lines.keySet());
                for (String key : keys) {
                    for (int i = 0; i < modelCount; i++) {
                        addOrAppend(lines, key, "");
                    }
                }
                Set<String> oldKeys = new HashSet<>(lines.keySet());
                oldKeys.removeAll(counter.keySet());
                for (String key : oldKeys) {
                    if (key.equals("")) continue;
                    addOrAppend(lines, key, "");
                }
                addOrAppend(lines, "", f.getName());
                for (Entry<String, Integer> entry : counter.entrySet()) {
                    addOrAppend(lines, entry.getKey(), entry.getValue() + "");
                }
                for (String line : lines.values()) {
                    assert (line.split(";").length == modelCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
                invalid.append(f.getName()).append("\n");
                count++;
            }
        }
        for (String s : sets) System.out.println(s);
        System.out.println(count + "\n");
        System.out.println(invalid);

        List<String> entries = new ArrayList<>(lines.keySet());
        Collections.sort(entries);
        StringBuilder file = new StringBuilder();
        for (String entry : entries) {
            file.append(entry).append(";").append(lines.get(entry)).append("\n");
        }
        try {
            FileWriter writer = new FileWriter(jsonDirPath + File.separator + "output.csv");
            writer.write(file.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Analyzes a diagram to count occurrences of specific stencil types and properties.
     *
     * @param d       the diagram to analyze
     * @param counter a map to track the counts of stencil types and properties
     */
    private static void countDiagram(Diagram d, HashMap<String, Integer> counter) {
        for (Shape shape : d.getShapes()) {
            if (shape.getStencilId() == null) continue;

            if (shape.getStencilId().equals("Task")) {
                int i = 0;
                for (Shape income : shape.getIncomings()) {
                    if (income.getStencilId() != null && income.getStencilId().equals("SequenceFlow")) i++;
                }
                if (i > 1) addOrIncrement(counter, "implicit XOR-join");

                int x = 0;
                for (Shape income : shape.getOutgoings()) {
                    if (income.getStencilId() != null && income.getStencilId().equals("SequenceFlow")) x++;
                }
                if (x > 1) addOrIncrement(counter, "implicit AND-split");

                if ("Standard".equalsIgnoreCase(shape.getProperty("looptype"))) {
                    addOrIncrement(counter, "Activity Looping");
                } else if ("MultiInstance".equalsIgnoreCase(shape.getProperty("looptype"))) {
                    addOrIncrement(counter, "Multiple Instance");
                } else if ("true".equals(shape.getProperty("iscompensation"))) {
                    addOrIncrement(counter, "Compensation");
                }
            }

            addOrIncrement(counter, shape.getStencilId());
        }
    }

    /**
     * Increments the count for a given stencil type in the counter map or initializes it if not present.
     *
     * @param counter the map storing counts of stencil types
     * @param id      the stencil type to increment
     */
    private static void addOrIncrement(HashMap<String, Integer> counter, String id) {
        counter.put(id, counter.getOrDefault(id, 0) + 1);
    }

    /**
     * Appends a value to an existing entry in the counter map, or initializes it if not present.
     *
     * @param counter the map storing stencil properties and their values
     * @param id      the stencil property key
     * @param entry   the value to append
     */
    private static void addOrAppend(HashMap<String, String> counter, String id, String entry) {
        counter.put(id, counter.getOrDefault(id, "") + ";" + entry);
    }

    /**
     * Provides usage instructions for the program.
     *
     * @return a usage message
     */
    private static String usage() {
        return "Usage: java Evaluator <directory_path>";
    }
}
