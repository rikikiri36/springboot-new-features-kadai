 const stripe = Stripe('pk_test_51PrIP7LEEp02wqa76jlJ50Fg9qjUjkqkq7EpTAR294raJmqmc2x7dbuVL0GcxMLi6wtgqssoy5CsVGFq92NAIojh00DY26ZVV2');
 const paymentButton = document.querySelector('#paymentButton');
 
 paymentButton.addEventListener('click', () => {
   stripe.redirectToCheckout({
     sessionId: sessionId
   })
 });