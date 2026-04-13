# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

### Building the Project
```bash
# Build the project with Maven
mvn clean install

# Build without running tests (faster)
mvn clean install -DskipTests

# Build with checkstyle validation (enforced during build)
mvn clean validate
```

### Running OpenPnP
```bash
# On Linux/Mac
./openpnp.sh

# On Windows
openpnp.bat

# Run directly with Java (after building)
java --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.desktop/java.awt=ALL-UNNAMED --add-opens=java.desktop/java.awt.color=ALL-UNNAMED -jar target/openpnp-gui-0.0.1-alpha-SNAPSHOT.jar
```

### Running Tests
```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=BasicJobTest

# Run tests with more memory (if needed)
mvn test -DargLine="-Xmx2048m"
```

### Development Commands
```bash
# Generate Eclipse project files with sources and JavaDocs
mvn eclipse:eclipse

# Check code style violations
mvn checkstyle:check

# Clean build artifacts
mvn clean
```

## Architecture Overview

### Core Components

1. **Machine Interface (`org.openpnp.spi`)**: Defines the abstract interfaces for all hardware components
   - `Machine`: Top-level machine interface
   - `Head`: Controls nozzles and cameras
   - `Nozzle`: Picks and places components
   - `Camera`: Vision system interface
   - `Feeder`: Component feeding mechanisms
   - `Driver`: Hardware communication layer

2. **Reference Implementation (`org.openpnp.machine.reference`)**: Default implementations of the SPI interfaces
   - `ReferenceMachine`: Standard machine implementation
   - `ReferenceHead`, `ReferenceNozzle`: Default head/nozzle implementations
   - Supports simulation mode for testing without hardware

3. **GUI Layer (`org.openpnp.gui`)**: Swing-based user interface
   - `MainFrame`: Main application window
   - Panel-based architecture for different views (JobPanel, MachineSetupPanel, etc.)
   - Uses BeansBinding for data binding between models and UI

4. **Vision Pipeline (`org.openpnp.vision.pipeline`)**: Computer vision processing
   - `CvPipeline`: Chain of image processing stages
   - Uses OpenCV for image processing
   - Configurable stages for different vision tasks

5. **Configuration System**: XML-based configuration using Simple XML framework
   - Machine configuration: `~/.openpnp2/machine.xml`
   - Parts/packages/boards stored separately
   - All configuration objects extend `AbstractModelObject`

### Key Design Patterns

- **Service Provider Interface (SPI)**: Hardware abstraction through interfaces
- **Factory Pattern**: Driver and component creation
- **Observer Pattern**: Configuration and event listeners
- **Pipeline Pattern**: Vision processing stages

## Project Structure

- `/src/main/java/org/openpnp/`: Main source code
  - `spi/`: Hardware abstraction interfaces
  - `machine/`: Machine implementations
  - `gui/`: User interface components
  - `vision/`: Computer vision code
  - `model/`: Data models
  - `util/`: Utility classes
- `/src/main/resources/`: Resources, icons, default configurations
- `/src/test/`: Unit tests
- Configuration stored in: `~/.openpnp2/` (or custom via `-DconfigDir`)

## Important Development Notes

- **Java Version**: Requires Java 11 or higher
- **Code Style**: Enforced via Checkstyle (no CRLF line endings, specific naming conventions)
- **Scripting Support**: JavaScript, Python (Jython), BeanShell supported for automation
- **Logging**: Uses TinyLog, logs written to `~/.openpnp2/log/OpenPnP.log`
- **Native Libraries**: OpenCV native libraries handled via custom Maven repository
- **Platform-specific**: Uses JVM flags for UI compatibility on different platforms