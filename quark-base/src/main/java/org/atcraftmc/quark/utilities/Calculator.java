package org.atcraftmc.quark.utilities;

import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.util.Stack;

/**
 * special thanks to ChatGPT.
 */
@QuarkModule(version = "1.0.0")
@QuarkCommand(name = "calc")
public final class Calculator extends CommandModule {

    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
        super.enable();
    }

    @Override
    public void disable(){
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
        super.disable();
    }

    public static double calculate(String expression) {
        char[] tokens = expression.toCharArray();
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == ' ')
                continue;

            if (Character.isDigit(tokens[i])) {
                StringBuilder sb = new StringBuilder();
                while (i < tokens.length && (Character.isDigit(tokens[i]) || tokens[i] == '.')) {
                    sb.append(tokens[i++]);
                }
                values.push(Double.parseDouble(sb.toString()));
                i--;
            } else if (tokens[i] == '(') {
                operators.push(tokens[i]);
            } else if (tokens[i] == ')') {
                while (operators.peek() != '(') {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop();
            } else if (tokens[i] == '+' || tokens[i] == '-' || tokens[i] == '*' || tokens[i] == '/') {
                while (!operators.isEmpty() && hasPrecedence(tokens[i], operators.peek())) {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(tokens[i]);
            }
        }

        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private static boolean hasPrecedence(char op1, char op2) {
        return (op2 != '(' && op2 != ')') && (getPrecedence(op1) <= getPrecedence(op2));
    }

    private static int getPrecedence(char operator) {
        return switch (operator) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            default -> -1;
        };
    }

    private static double applyOperator(char operator, double b, double a) {
        return switch (operator) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> {
                if (b == 0) {
                    throw new ArithmeticException("Cannot divide by zero");
                }
                yield a / b;
            }
            default -> 0;
        };
    }


    @Override
    public void execute(CommandExecution context) {
        String exp=context.requireArgumentAt(0);

        this.getLanguage().sendMessage(context.getSender(), "calc", exp, calculate(exp));
    }
}
