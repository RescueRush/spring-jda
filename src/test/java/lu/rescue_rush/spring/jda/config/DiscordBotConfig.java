package lu.rescue_rush.spring.jda.config;

import lu.pcy113.pclib.config.ConfigLoader.ConfigContainer;
import lu.pcy113.pclib.config.ConfigLoader.ConfigProp;

public class DiscordBotConfig implements ConfigContainer {

	@ConfigProp("username")
	public String username;

	@ConfigProp("author.name")
	public String authorName;

	@ConfigProp("author.icon")
	public String authorIcon;

	@ConfigProp("author.url")
	public String authorUrl;

	@ConfigProp("token")
	public String token;

	@ConfigProp("server.id")
	public String serverId;

	@ConfigProp("server.channels.logs")
	public String channelLogId;

	public DiscordBotConfig() {
	}

	public DiscordBotConfig(String username, String authorName, String authorIcon, String authorUrl, String token, String serverId,
			String channelLogId) {
		this.username = username;
		this.authorName = authorName;
		this.authorIcon = authorIcon;
		this.authorUrl = authorUrl;
		this.token = token;
		this.serverId = serverId;
		this.channelLogId = channelLogId;
	}

	@Override
	public String toString() {
		return "DiscordBotConfig [username=" + username + ", authorName=" + authorName + ", authorIcon=" + authorIcon + ", authorUrl="
				+ authorUrl + ", token=" + token + ", serverId=" + serverId + ", channelLogId=" + channelLogId + "]";
	}

}
