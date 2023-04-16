import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class AcheteurAgent extends Agent {
    private int argentDePoche;
    private int meilleurOffre;
    private int personalBest;

    protected void setup() {
        // Enregistrer l'agent comme un acheteur
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Acheteur");
        sd.setName("Enchères");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }


        // Récupérer le prix maximal depuis l'argument du programme
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            argentDePoche = Integer.parseInt((String) args[0]);
        } else {
            // Si l'argument est manquant, définir une valeur par défaut
            argentDePoche = 50;
        }

        // Initialiser le meilleur offre et le pas d'enchère
        assert args != null;
        meilleurOffre = Integer.parseInt((String) args[1]);


        // Ajouter un comportement périodique pour faire des offres

        addBehaviour(new TickerBehaviour(this, 2000) {
            public void onTick() {
                if (meilleurOffre <= argentDePoche) {
                    // Générer une offre aléatoire supérieure au meilleur offre actuel sans dépasser son argent de poche
                    int nouvelleOffre = (int) (meilleurOffre + (Math.random() * (argentDePoche/2-meilleurOffre)));

                    if (nouvelleOffre <= argentDePoche && nouvelleOffre!=personalBest) {
                        // Envoyer l'offre au vendeur
                        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                        msg.addReceiver(getAID("vendeur"));
                        msg.setContent(String.valueOf(nouvelleOffre));

                        send(msg);

                        // Attendre la réponse du vendeur
                        ACLMessage reply = receive();
                        if (reply != null) {
                            // Mettre à jour le meilleur offre si la réponse est valide
                            int prix = Integer.parseInt(reply.getContent());
                           // System.out.println(getAID().getLocalName()+ " Has received the msg");
                            if (prix > meilleurOffre) {
                                meilleurOffre = prix;
                                //System.out.println(getAID().getLocalName() + " a fait une offre de " + prix + "$");
                            }
                        } else {
                            //block();
                         //System.out.println( getAID().getLocalName() + " n'a proposé aucune offre.");
                        }
                    }
                } else {
                    // Arrêter le comportement si le prix maximal est atteint
                    System.out.println(getAID().getLocalName() + " ne fait plus d'offres");
                    stop();
                }
            }
        });
    }}
