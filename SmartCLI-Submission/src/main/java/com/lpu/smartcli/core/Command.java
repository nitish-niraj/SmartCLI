package com.lpu.smartcli.core;
import com.lpu.smartcli.data.FileSystem;

public interface Command {
    void execute(String[] args, FileSystem fs);
    String getDescription();
}
