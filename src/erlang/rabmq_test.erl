-module(rabmq_test).

-include("amqp_client/include/amqp_client.hrl").

-record(rabbit_client , { connection ,channel}).

-export([start/0,sendloop/1]).

%%
%%export functions
%%
start() ->
    Queue = <<"kun_Q">>,
    {ok,Client} = connect(),
    Channel = Client#rabbit_client.channel,
    spawn( fun() -> loop(Channel,Queue,0) end ),
    self().

sendloop(N) ->
    {ok,Client} = connect(),
    publish_msg(Client,N).

%%
%%internal functions
%%
connect()  ->
    {ok, Connection}=amqp_connection:start(#amqp_params_network{}),
    {ok, Channel}  = amqp_connection:open_channel(Connection),

    Client = #rabbit_client{
        connection = Connection,
        channel = Channel
    },
    {ok, Client}.

loop(Channel,Queue,N) ->
    amqp_channel:call(Channel, #'basic.consume'{queue = Queue,no_ack=true}),
    receive
        #'basic.consume_ok'{} ->
            loop(Channel,Queue,N);

        #'basic.cancel_ok'{} ->
            ok;

        {#'basic.deliver'{delivery_tag = Tag}, Content} ->
            %%io:fwrite(" [x] Received ~p~n",[Payload]),
            %%amqp_channel:cast(Channel, #'basic.ack'{delivery_tag = Tag}),
            %%N = N + 1,
            %%count(N),
            loop(Channel,Queue,N)
    end.

count(100000) ->
    {ok,M_Time} = current_time_millis(),
    io:fwrite(" [x] time:~p~n",[M_Time]);
count(N) ->
    ok.

current_time_millis() ->
    {M,S,MS} = erlang:now(),
    {ok,{M*1000000*1000 + S*1000 + MS/1000}}.

publish_msg(Client,N) ->
    Channel = Client#rabbit_client.channel,
    Payload = <<"Hello kun">>,
    Exchange = #'basic.publish'{exchange = <<"kun_exchange">>},
    loop_send(Channel,Exchange,Payload,N).

loop_send(Channel,Exchange,Payload,0) ->
    ok;
loop_send(Channel,Exchange,Payload,N) ->
    amqp_channel:cast(Channel, Exchange, #'amqp_msg'{payload=Payload}),
    loop_send(Channel,Exchange,Payload,N-1).