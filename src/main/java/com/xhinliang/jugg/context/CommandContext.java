package com.xhinliang.jugg.context;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author xhinliang
 */
public class CommandContext {

    private final JuggUser juggUser;

    private final String command;

    private String result;

    private boolean shouldEnd;

    public CommandContext(JuggUser juggUser, String command) {
        this.juggUser = juggUser;
        this.command = command;
    }

    @Nullable
    public String getResult() {
        return result;
    }

    public void setResult(@Nullable String result) {
        this.result = result;
    }

    public boolean isShouldEnd() {
        return shouldEnd;
    }

    public void setShouldEnd(boolean shouldEnd) {
        this.shouldEnd = shouldEnd;
    }

    @Nonnull
    public JuggUser getJuggUser() {
        return juggUser;
    }

    @Nonnull
    public String getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommandContext that = (CommandContext) o;
        return shouldEnd == that.shouldEnd && Objects.equals(juggUser, that.juggUser) && Objects.equals(command, that.command)
                && Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(juggUser, command, result, shouldEnd);
    }
}
