package parser;

import command.Command;

import java.text.ParseException;
import java.util.Collection;

public interface Parser {
    public Collection<Command> parse(String input) throws ParseException;
}
