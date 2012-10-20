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
    spawn( fun() -> loop(Channel,Queue) end ),
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

loop(Channel,Queue) ->
    amqp_channel:call(Channel, #'basic.consume'{queue = Queue,no_ack=true}),
    receive
        #'basic.consume_ok'{} ->
            loop(Channel,Queue);

        #'basic.cancel_ok'{} ->
            ok;

        {#'basic.deliver'{delivery_tag = Tag}, Content} ->
            %%io:fwrite(" [x] Received ~p~n",[Payload]),
            %%amqp_channel:cast(Channel, #'basic.ack'{delivery_tag = Tag}),
            loop(Channel,Queue)
    end.

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