package ar.edu.itba.sds;

import ar.edu.itba.sds.engine.*;
import ar.edu.itba.sds.tools.PostProcessor;
import ar.edu.itba.sds.tools.Time;

import java.io.IOException;
import java.util.Iterator;

public class App {
    public static void main(String[] args)  {

        Field field = new Field(11,10,1, 40);
        Iterator<Time> timeIterator = field.iterator();
        try(PostProcessor postProcessor = new PostProcessor("dynamicOutput.txt")) {
            postProcessor.processEpoch(field.getCurrentTime()); //t = 0
            timeIterator.forEachRemaining(postProcessor::processEpoch);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
