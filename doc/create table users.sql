create database tb;


create table users
(
    id       serial  not null,
    email    varchar not null,
    password varchar not null
);

create table teammembers
(
    team team not null
) inherit (users);

create table admins
(
    
) inherit (users);

create table screens
(
    id      serial  not null,
    mac     varchar not null,
    logged  boolean,
    site    sites
);

create table waitingscreens
(
    mac  varchar not null,
    code varchar not null
);

create table fluxes
(
    id       serial  not null,
    name     varchar,
    url      varchar not null,
    duration integer
);

create table locatedfluxes
(
    sites   sites
) inherit (fluxes);

create table generalfluxes
(

) inherit (fluxes);

create table fallbackfluxes
(

) inherit (fluxes);

create table diffusers
(
    id       serial  not null,
    validity date,
    name     varchar not null,
    fluxes   fluxes not null
);

create table runningdiffusers
(
    screens screens[] not null
) inherit (diffusers);

create table schedules
(
    id     serial  not null,
    fluxes fluxes[],
    name   varchar not null
);

create table runningschedules
(
    screens screens[] not null
) inherit (schedules);

create table screengroups
(
    id      serial  not null,
    screens screens[] not null
);

create table teams
(
    id           serial  not null,
    name         varchar not null,
    screengroups screengroups[],
    schedules    schedules[],
    diffusers    diffusers[],
    fluxes       fluxes[]
);

create table sites
(
    id   serial  not null,
    name varchar not null
);

