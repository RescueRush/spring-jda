package lu.rescue_rush.spring.jda.command.slash;

public interface SubSlashCommandExecutor extends SlashCommandExecutor {

	Class<? extends SlashCommandExecutor> getCommandClass();

}
