package org.tbstcraft.quark.command.tree;

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;

public abstract class BuilderCommand {
    public abstract void build(Node root);

    public static abstract class Node {
        final int layer;
        private final HashMap<String, Node> nodes = new HashMap<>();

        private Node(int layer) {
            this.layer = layer;
        }

        public abstract void execute(CommandSender sender, String[] args);

        public abstract void tab(CommandSender sender, String[] args, List<String> tabList);

        public Node passBranch(String id) {
            return this.nodes.computeIfAbsent(id, s -> new PassNode(layer + 1));
        }

        public void endBranch(String id, NodeCommandExecutor executor) {
            this.nodes.computeIfAbsent(id, s -> new EndNode(layer + 1, executor, null));
        }

        protected HashMap<String, Node> getNodes() {
            return this.nodes;
        }

        public Node pass(String id, NodeTabCompleter tab) {
            return this.nodes.computeIfAbsent(id, s -> new PassNodeTab(layer + 1, tab));
        }

        public void end(String id, NodeTabCompleter tab, NodeCommandExecutor executor) {

        }


        public void execute(String id, NodeTabCompleter tab,NodeCommandExecutor executor){

        }
    }

    private static final class PassNode extends Node {
        private PassNode(int layer) {
            super(layer);
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            this.getNodes().get(args[0]);
        }

        @Override
        public void tab(CommandSender sender, String[] args, List<String> tabList) {
            tabList.addAll(this.getNodes().keySet());
        }
    }

    private static final class PassNodeTab extends Node {
        private final NodeTabCompleter tab;

        private PassNodeTab(int layer, NodeTabCompleter tab) {
            super(layer);
            this.tab = tab;
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            this.getNodes().get(args[0]);
        }

        @Override
        public void tab(CommandSender sender, String[] args, List<String> tabList) {
            tab.tab(sender, args,0, tabList);
        }
    }

    private static class EndNode extends Node {
        private final NodeCommandExecutor executor;
        private final NodeTabCompleter completer;

        private EndNode(int layer, NodeCommandExecutor executor, NodeTabCompleter completer) {
            super(layer);
            this.executor = executor;
            this.completer = completer;
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            this.executor.execute(sender, args);
        }

        @Override
        public void tab(CommandSender sender, String[] args, List<String> tabList) {
            this.completer.tab(sender, args, args.length - this.layer, tabList);
        }
    }
}
