package valoeghese.salt;

import java.util.List;
import java.util.Map;

public record Circuit(Map<String, Node> nodes, List<Connection> connections, Properties properties) {
}
