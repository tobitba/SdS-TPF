package ar.edu.itba.sds.tools;

import ar.edu.itba.sds.engine.Agent;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

public class PostProcessor implements Closeable {
    private static final String OUTPUT_FILE_NAME = "dynamicOutput.txt";
    private static final String CIVILIANS = "CIVILIANS\n";
    private static final String DOCTORS = "DOCTORS\n";
    private static final String ZOMBIES = "ZOMBIES\n";
    private final BufferedWriter writer;

    public PostProcessor(String outputName) {
        try {
            if (outputName == null)
                outputName = OUTPUT_FILE_NAME;
            writer = new BufferedWriter(new FileWriter(outputName));
        } catch (IOException e) {
            throw new RuntimeException("Error opening file");
        }
    }

    public void processEpoch(Time time) {
        try {
            writer.write(String.valueOf(time.time()));
            writer.newLine();
            writer.write(CIVILIANS);
            time.civilians().forEach(this::processParticle);
            writer.write(DOCTORS);
            time.doctors().forEach(this::processParticle);
            writer.write(ZOMBIES);
            time.zombies().forEach(this::processParticle);
            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException("Error writing on output file");
        }
    }

    private void processParticle(Agent agent) {
        try {
            writer.write(agent.toString());
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing on output file");
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}