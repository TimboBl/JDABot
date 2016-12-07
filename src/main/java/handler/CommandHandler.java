package handler;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import enums.TournamentMode;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import services.CommandService;
import services.IJustLostTheGameService;
import services.PollService;
import services.TournamentService;

/**
 * This class takes all user input and processes it. It holds all commands but no knowledge about them.
 * @author CkFreak
 * @version 1.12.2016
 */
public class CommandHandler implements Observer
{

    private final static String SALT_EMOJI = "src/main/res/saltEmoji.jpg";

    private final static String SUGAR_EMOJI = "src/main/res/zucker.jpg";

    private final static String JUST_RIGHT_MEME = "src/main/res/justright.gif";

    private final static String FUCK_OFF = "src/main/res/fuckoff.gif";

    private final static String LIKE = "src/main/res/like.png";

    private final static String BITCH_PLEASE = "src/main/res/bitchplease.jpg";

    private final static String HELP_COMMAND_FILE = "src/main/res/commands.txt";

    private static final String THE_GAME_INITIALIZATION = "The Game has been initialized!";

    private static final String INSUFICENT_RIGHTS_MESSAGE = "You do not have sufficent permissions to do that";

    /**
     * The Service that executes the commands
     */
    private CommandService _commander;

    /**
     * The JDA instance of this bot 
     */
    private JDA _jda;

    /**
     * The LAVA Player for music
     */
    private AudioHandler _player;

    /**
     * The IJustLostTheGameService that makes sure the game is lost in random intervals (Dammit I just lost the Game)
     */
    private IJustLostTheGameService _loseGameService;

    /**
     * The PollService of this CommandHandler
     */
    private PollService _pollService;

    /**
     * A MessageReceivedEvent
     */
    private MessageReceivedEvent _event;

    /**
     * A Tournament Service that starts Tournaments
     */
    private TournamentService _tournamentService;

    /**
     * The only Instance of this CLass in the JVM
     */
    private static CommandHandler _instance;

    /**
     * Initializes a CommandHandler and all its services
     */
    private CommandHandler(JDA jda)
    {
        _event = null;
        _jda = jda;
        _commander = new CommandService(_event, jda);
        _pollService = new PollService();
        _loseGameService = new IJustLostTheGameService();
        _loseGameService.addObserver(this);
        _player = new AudioHandler();
    }

    /**
     * A Method that catches every Message and checks for the command escape charackter
     * 
     * @param event The MessageReceivedEvent with the message inside
     */
    public void handleIncomingMessages(MessageReceivedEvent event)
    {
        _event = event;
        _commander.initializeRoles(event);

        String message = event.getMessage()
            .getContent();

        if (message.startsWith("#"))
        {
            //splits the message at spaces
            String[] messageContent = message.split("\\s+");
            event.getMessage()
                .deleteMessage();
            event.getChannel()
                .sendTyping()
                .queue();
            ;

            switch (messageContent[0].substring(1))
            {
            case "hello":
                _commander.replyToHello(event);
                break;

            case "help":
                _commander.getHelpCommands(event, HELP_COMMAND_FILE);
                break;

            case "admin":
                event.getChannel()
                    .sendMessage(_commander.getAdmin(event)
                        .toString());
                break;

            case "delete":
                int amount = Integer.valueOf(messageContent[1]);
                _commander.deleteChannelMessages(event, amount);
                event.getChannel()
                    .sendMessage(
                            "Es wurden " + amount + " Nachrichten gelöscht.")
                    .queue();
                break;

            case "userInfo":
                String messageForUser = _commander.getUserInfo(event,
                        messageContent);
                event.getChannel()
                    .sendMessage(messageForUser)
                    .queue();
                break;

            case "salt":
                _commander.sendEmoji(event, SALT_EMOJI);
                break;

            case "justright":
                _commander.sendEmoji(event, JUST_RIGHT_MEME);
                break;

            case "sugar":
                _commander.sendEmoji(event, SUGAR_EMOJI);
                break;

            case "fuckoff":
                _commander.sendEmoji(event, FUCK_OFF);
                break;

            case "like":
                _commander.sendEmoji(event, LIKE);
                break;

            case "please":
                event.getChannel()
                    .sendTyping()
                    .queue();
                _commander.sendEmoji(event, BITCH_PLEASE);
                break;

            //            case "deleteAll":
            //                _commander.deleteAllMessages(event);
            //                break;

            case "add":
                event.getChannel()
                    .sendMessage(
                            "Nicht ungeduldig werden, das kann ein wenig dauern. Es wird begonnen, die URL zu verarbeiten!")
                    .queue();

                _player.registerNewTrack(messageContent[1], event);
                break;

            case "play":

                if (_player.getPlaylist() == null)
                {
                    event.getChannel()
                        .sendMessage(
                                "Currently the playlist is empty. Please use #add to add Songs to the playlist")
                        .queue();
                }
                _player.startPlaying();
                break;

            case "pause":
                _player.pausePlayer();
                event.getChannel()
                    .sendMessage("Playback has been paused")
                    .queue();
                break;

            case "volume":
                _player.setVolume(Integer.parseInt(messageContent[1]));
                break;

            case "stop":
                _player.stopPlayer();
                break;

            case "skip":
                _player.playNextSong();
                break;

            case "playlist":
                event.getChannel()
                    .sendMessage(_player.getPlaylist())
                    .queue();
                break;

            case "restart":
                _player.restartSong();
                break;

            case "reset":
                if (_commander.isAdmin(event.getAuthor(), event))
                {
                    _player.resetPlayer();
                }
                else
                {
                    event.getChannel()
                        .sendMessage(INSUFICENT_RIGHTS_MESSAGE)
                        .queue();
                    ;
                }
                break;

            case "shuffle":
                boolean enable = false;
                if (messageContent[1].equals("1"))
                {
                    enable = true;
                }
                else if (messageContent[1].equals("0"))
                {
                    enable = false;
                }

                _player.isShuffle(enable);
                break;

            case "join":
                //                _player.joinChannel(messageContent[1], event);
                break;

            case "leave":
                //                _player.leaveChannel(event);
                break;

            case "shutdown":
                event.getChannel().sendMessage("Going down for maintenance").queue();
                _commander.reagiereAufShutdown(event);
                break;

            case "resume":
                _player.resumePlayer();
                break;

            case "mods":
                event.getChannel()
                    .sendMessage(_commander.getMods(event))
                    .queue();
                break;

            case "changeGame":
                event.getMessage()
                    .deleteMessage()
                    .queue();

                _commander.changeGame(_jda, getGameName(messageContent));
                break;

            case "startPVote":
                _pollService.startPPoll(getPollName(messageContent),
                        event.getAuthor(), _event, getOptions(messageContent));
                break;

            case "startAVote":
                _pollService.startAPoll(getPollName(messageContent),
                        event.getAuthor(), event, getOptions(messageContent));
                break;

            case "getStatus":
                event.getChannel()
                    .sendMessage(
                            _pollService.getStatus(getPollName(messageContent)))
                    .queue();
                break;

            case "endVote":
                event.getChannel()
                    .sendMessage(_pollService.endPoll(
                            getPollName(messageContent), event.getAuthor()))
                    .queue();
                break;

            case "vote":
                event.getChannel()
                    .sendMessage(_pollService.vote(getPollName(messageContent),
                            getOptions(messageContent), event.getAuthor(),
                            event))
                    .queue();
                break;

            case "listVotes":
                event.getChannel()
                    .sendMessage(_pollService.getCurrentVotes())
                    .queue();
                ;
                break;

            case "startGame":
                _loseGameService.executeGameLoss(event);
                event.getChannel()
                    .sendMessage(THE_GAME_INITIALIZATION)
                    .queue();
                break;

            case "gameData":
                event.getChannel()
                    .sendMessage("Next Game Loss:" + "\n"
                            + _loseGameService.getNextGameLoss()
                                .toString()
                            + "\n Now: \n" + _loseGameService.getNow()
                                .toString())
                    .queue();
                break;

            //TODO Command richtig implementieren
            case "startTournament":
                TournamentMode mode = null;
                switch (getPollName(messageContent).toLowerCase())
                {
                    case "single elimination":
                        mode = TournamentMode.SINGE_ELIMINATION;
                        break;
                    case "double elimination":
                        mode = TournamentMode.DOUBLE_ELIMINATION;
                        break;
                    case "tripple elimination":
                        mode = TournamentMode.TRIPLE_ELIMINATION;
                        break;
                    case "round robin":
                        mode = TournamentMode.ROUND_ROBIN;
                        break;
                        default:
                            event.getChannel().sendMessage("The Tournament type does not match any known modes").queue();
                            break;

                }

                event.getChannel()
                    .sendMessage(_tournamentService
                        .initializeTournament(mode , getOptions(messageContent)))
                    .queue();

            default:
                event.getChannel()
                    .sendMessage("Sorry but this command is not defined!")
                    .queue();
                break;

            }
        }
    }


    /**
     * Returns the Game the Bot wants to play next
     * @param messageContent The message contents without white spaces
     * @return The name of the next game as String
     */
    private String getGameName(String[] messageContent)
    {
        String game = "";
        int length = messageContent.length;
        for (int i = 1; i <= length - 1; ++i)
        {
            game += " " + messageContent[i];
        }
        return game;
    }

    /**
     * Returns the choosens poll's name
     * @param messageContent the message contents without white spaces
     * @return the poll's name
     */
    private String getPollName(String[] messageContent)
    {
        String name = "";
        int index = 1;
        int i = 1;
        do
        {
            name += " " + messageContent[i] + " ";
            name = name.replaceAll("_", "");
            ++index;
            ++i;
        }
        while (!messageContent[index - 1].contains("_"));
        return name;
    }

    /**
     * Gives the desired poll options
     * @param messageContent the message content without white spaces
     * @return A list with poll options
     */
    private ArrayList<String> getOptions(String[] messageContent)
    {
        ArrayList<String> options = new ArrayList<>();
        String option = "";
        int such = 1;

        //searching for the first "_" so it's definetly not in the options list.
        do
        {
            ++such;
        }
        while (!messageContent[such].contains("_"));

        for (int i = such; i <= messageContent.length - 1; ++i)
        {
            System.out.println(messageContent[i]);
            option += " " + messageContent[i] + " ";
            if (messageContent[i].contains("_"))
            {
                option = option.replaceAll("_", "");
                options.add(such, option);
                option = "";
            }

        }
        return options;
    }

    /**
     * Gives back the only instance of this class in the JVM
     * @return the only instance of this class in the JVM
     */
    public static CommandHandler getInstance(JDA jda)
    {
        if (_instance == null)
        {
            _instance = new CommandHandler(jda);
        }
        return _instance;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        _loseGameService.executeGameLoss(_event);
    }

}
