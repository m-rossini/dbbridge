# Sample DSL
[when]Type is {type}.= m : Message( status == {type}, msg : message )
[then]Show {text}.=println( {text} );
[then]Set Message Type to {type}.=m.setStatus( {type} );
[then]Set Message Text to {text}.=m.setMessage( {text} );
[then]Update {obj}.=modify( {obj} );
[then]MessageObj=m
[then]message text=msg
[when]Good Bye=Message.GOODBYE
[then]Good Bye=Message.GOODBYE
[when]Hello=Message.HELLO
[then]Add result {text}.=results.add({text});