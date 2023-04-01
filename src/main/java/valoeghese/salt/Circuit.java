package valoeghese.salt;

import java.util.List;

public record Circuit(List<Node> nodes, List<Connection> connections, Properties properties) {
}
