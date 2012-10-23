-module(rabmq_test).

-include("amqp_client/include/amqp_client.hrl").

-record(rabbit_client , { connection ,channel}).

-export([start/2,sendloop/3]).

%%
%%export functions
%%
start(Concurrency,Queue) ->
    {ok,Client} = connect(),
    Channel = Client#rabbit_client.channel, 
    loop_start_channel(Queue,Client,Concurrency),
    self().

sendloop(Payload,Exchange,N) ->
    {ok,Client} = connect(),
    publish_msg(Payload,Exchange,Client,N),
    amqp_connection:close(Client#rabbit_client.connection).

%%
%%internal functions
%%
loop_start_channel(Queue,Client,1) ->
    Consumer = spawn(fun() -> loop() end),
    amqp_channel:subscribe(Client#rabbit_client.channel, #'basic.consume'{queue = Queue,no_ack=true}, Consumer),
    ok;
loop_start_channel(Queue,Client,N) ->
    {ok,Channel} = amqp_connection:open_channel(Client#rabbit_client.connection),
    Consumer = spawn(fun() -> loop() end),
    amqp_channel:subscribe(Channel, #'basic.consume'{queue = Queue,no_ack=true}, Consumer),
    loop_start_channel(Queue,Client,N-1).

connect()  ->
    {ok, Connection}=amqp_connection:start(#amqp_params_network{}),
    {ok, Channel}  = amqp_connection:open_channel(Connection),

    Client = #rabbit_client{
        connection = Connection,
        channel = Channel
    },
    {ok, Client}.

loop() ->
    %%amqp_channel:call(Channel, #'basic.consume'{queue = Queue,no_ack=true}),
    receive
        #'basic.consume_ok'{} ->
            loop();

        #'basic.cancel_ok'{} ->
            ok;

        {#'basic.deliver'{delivery_tag = Tag}, Content} ->
            %%
            %%do something to msg
            %%
            %%if no_ack=false use this code to acknowledge
            %%amqp_channel:cast(Channel, #'basic.ack'{delivery_tag = Tag}),
            %%
            loop()
    end.

current_time_millis() ->
    {M,S,MS} = erlang:now(),
    {ok,M*1000000*1000 + S*1000 + MS/1000}.

current_time_second() ->
    {M,S,_} = erlang:now(),
    {ok,M*1000000 + S}.

publish_msg(Payload,ExchangeStr,Client,N) ->
    Channel = Client#rabbit_client.channel,
    Exchange = #'basic.publish'{exchange = ExchangeStr},
    loop_send(Channel,Exchange,Payload,N).

loop_send(Channel,Exchange,Payload,0) ->
    ok;
loop_send(Channel,Exchange,Payload,N) ->
    amqp_channel:cast(Channel, Exchange, #'amqp_msg'{payload=Payload}),
    loop_send(Channel,Exchange,Payload,N-1).