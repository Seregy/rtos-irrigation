package core;

import command.Command;
import parser.Lexer;
import parser.TokenParser;

import java.io.*;
import java.text.ParseException;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String... args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter path of the file to be parsed:");
        String path = scanner.nextLine();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-16"))) {
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = reader.readLine();
            }

            String result = sb.toString();
            List<Command> commands = new TokenParser(new Lexer()).parse(result);
            for (Command command : commands) {
                System.out.println(command.getName());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
