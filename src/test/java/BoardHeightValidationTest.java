import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.openpnp.machine.reference.ReferenceMachine;
import org.openpnp.machine.reference.ReferencePnpJobProcessor;
import org.openpnp.machine.reference.driver.test.TestDriver;
import org.openpnp.model.Board;
import org.openpnp.model.BoardLocation;
import org.openpnp.model.Configuration;
import org.openpnp.model.Job;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.model.Package;
import org.openpnp.model.Part;
import org.openpnp.model.Placement;
import org.openpnp.spi.JobProcessor.JobProcessorException;

import com.google.common.io.Files;

public class BoardHeightValidationTest {
    
    /**
     * Tests that the job processor throws an error when board height is set to 0.
     * 
     * @throws Exception
     */
    @Test
    public void testBoardHeightZeroValidation() throws Exception {
        File workingDirectory = Files.createTempDir();
        workingDirectory = new File(workingDirectory, ".openpnp");
        
        // Copy the required configuration files
        FileUtils.copyDirectory(new File("src/test/resources/config/BasicJobTest"), workingDirectory);
        Configuration.initialize(workingDirectory);
        Configuration.get().load();

        // Get the machine and job processor
        ReferenceMachine machine = (ReferenceMachine) Configuration.get().getMachine();
        ReferencePnpJobProcessor jobProcessor = (ReferencePnpJobProcessor) machine.getPnpJobProcessor();

        // Create a simple job with board height set to 0
        Job job = new Job();
        
        // Create a board without any placements to simplify the test
        Board board = new Board();
        board.setName("TEST_BOARD");
        
        // Add a simple placement so the job processor will check the board
        Part part = Configuration.get().getParts().get(0); // Use existing part from test config
        if (part != null) {
            Placement placement = new Placement("P1");
            placement.setPart(part);
            placement.setLocation(new Location(LengthUnit.Millimeters, 10, 10, 0, 0));
            board.addPlacement(placement);
        }
        
        // Create board location with Z height set to 0 (this should trigger the error)
        BoardLocation boardLocation = new BoardLocation(board);
        boardLocation.setLocation(new Location(LengthUnit.Millimeters, 0, 0, 0, 0)); // Z = 0
        boardLocation.setLocallyEnabled(true);
        
        job.addBoardOrPanelLocation(boardLocation);
        
        // This should throw a JobProcessorException due to board height being 0
        JobProcessorException exception = Assertions.assertThrows(JobProcessorException.class, () -> {
            jobProcessor.initialize(job);
        });
        
        // Verify the error message contains the expected text
        Assertions.assertTrue(exception.getMessage().contains("Board height (Z) is not set"));
        Assertions.assertTrue(exception.getMessage().contains("TEST_BOARD"));
        
        System.out.println("Test passed: Board height validation correctly triggered error - " + exception.getMessage());
    }
    
    /**
     * Tests that the job processor does NOT throw an error when board height is properly set.
     * 
     * @throws Exception
     */
    @Test
    public void testBoardHeightValidValidation() throws Exception {
        File workingDirectory = Files.createTempDir();
        workingDirectory = new File(workingDirectory, ".openpnp");
        
        // Copy the required configuration files
        FileUtils.copyDirectory(new File("src/test/resources/config/BasicJobTest"), workingDirectory);
        Configuration.initialize(workingDirectory);
        Configuration.get().load();

        // Get the machine and job processor
        ReferenceMachine machine = (ReferenceMachine) Configuration.get().getMachine();
        ReferencePnpJobProcessor jobProcessor = (ReferencePnpJobProcessor) machine.getPnpJobProcessor();

        // Create a simple job with proper board height
        Job job = new Job();
        
        // Create a board without any placements to simplify the test
        Board board = new Board();
        board.setName("TEST_BOARD");
        
        // Add a simple placement so the job processor will check the board
        Part part = Configuration.get().getParts().get(0); // Use existing part from test config
        if (part != null) {
            Placement placement = new Placement("P1");
            placement.setPart(part);
            placement.setLocation(new Location(LengthUnit.Millimeters, 10, 10, 0, 0));
            board.addPlacement(placement);
        }
        
        // Create board location with proper Z height (this should NOT trigger an error)
        BoardLocation boardLocation = new BoardLocation(board);
        boardLocation.setLocation(new Location(LengthUnit.Millimeters, 0, 0, 1.6, 0)); // Z = 1.6mm (typical PCB thickness)
        boardLocation.setLocallyEnabled(true);
        
        job.addBoardOrPanelLocation(boardLocation);
        
        // This should NOT throw an exception since board height is properly set
        Assertions.assertDoesNotThrow(() -> {
            jobProcessor.initialize(job);
        });
        
        System.out.println("Test passed: Board height validation correctly allowed valid board height");
    }
}