package com.johannesbrodwall.projectweek.projects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class Project {

    @Getter @Setter
    public Integer id;

    @Getter
    private final String key, name;


}
