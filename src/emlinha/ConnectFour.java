package emlinha;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

enum Piece
{
	Red,
	Blue,
	None
}
class Slot extends JButton
{
	public int i, j;
	public Piece peca = Piece.None;
	
	public Slot(int i, int j)/*definicao de jogadores como I e J no tabuleiro*/
	{
		this.i = i;
		this.j = j;
		setOpaque(true);
		setColor();
	}
	public void setPiece(Piece peca)
	{
		this.peca = peca;
		setColor();
	}
	public void setColor()/*define a cor para cada cada jogador*/
	{
		switch(peca)
		{
			case Red:
				setBackground(Color.red);
				break;
			case Blue:
				setBackground(Color.blue);
				break;
			case None:
				setBackground(Color.white);/*cor branca para os espacos em branco*/
				break;
		}
	}
}

class Arvore
{
	public int value;
	Slot[][] slots;
	private ArrayList<Integer> melhoresJogadas;
	Slot prev = null;
	int profundidade;
	static int maxprofundidade = 6;/*maxima jogadas possiveis,jogas verticais */
	
	public Arvore(Slot[][] slots, int profundidade)
	{
		this.slots = slots;
		this.melhoresJogadas = new ArrayList<Integer>();
		this.profundidade = profundidade;
		this.value = getValue();
		
		if(profundidade < maxprofundidade && this.value < 100 && this.value > -100 )
		{
			ArrayList<Integer> possibilidades = new ArrayList<Integer>();
			for(int i = 0; i < 7; i++)
				if(slots[i][0].peca == Piece.None)
					possibilidades.add(i);
			
			for(int i = 0; i < possibilidades.size(); i++)
			{
				insertTo(slots[possibilidades.get(i)][0]);
				Arvore child = new Arvore(slots, profundidade+1);
				prev.setPiece(Piece.None);
				
				if(i == 0)
				{
					melhoresJogadas.add(possibilidades.get(i));
					value = child.value;
				}
				else if(profundidade % 2 == 0)
				{
					if(value < child.value)
					{
						melhoresJogadas.clear();
						melhoresJogadas.add(possibilidades.get(i));
						this.value = child.value;
					}
					else if(value == child.value)
						melhoresJogadas.add(possibilidades.get(i));
				}
				else if(profundidade % 2 == 1)
				{
					if(value > child.value)
					{
						melhoresJogadas.clear();
						melhoresJogadas.add(possibilidades.get(i));
						this.value = child.value;
					}
					else if(value == child.value)
						melhoresJogadas.add(possibilidades.get(i));
				}
			}
		}
		else
		{
			this.value = getValue();
		}
	}
	
	void printSlots()
	{
		for(int j = 0; j < 6; j++)
		{
			for(int i = 0; i < 7; i++)
			{
				switch(slots[i][j].peca)
				{
					case Blue: System.out.print("B"); break;
					case Red: System.out.print("R"); break;
					default: System.out.print("-"); break;
				}
			}
			System.out.println();
		}
	}
	
	void insertTo(Slot slot)
	{
		if(slot.peca != Piece.None)
			return;
		
		int i = slot.i;
		int j = slot.j;
		
		while(j < slots[0].length-1 && slots[i][j+1].peca == Piece.None)
			j++;
		
		if(profundidade % 2 == 0)
			slots[i][j].setPiece(Piece.Red);
		else
			slots[i][j].setPiece(Piece.Blue);
		prev = slots[i][j];
	}
	
	public int getX()
	{
		int random = (int)(Math.random() * 100) % melhoresJogadas.size();
		return melhoresJogadas.get(random);
	}
	
	public int getValue()
	{
		int value = 0;
		for(int j = 0; j < 6; j++)
		{
			for(int i = 0; i < 7; i++)
			{
				if(slots[i][j].peca != Piece.None)
				{
					if(slots[i][j].peca == Piece.Red)
					{
						value += possiveisconexoes(i, j) * (maxprofundidade - this.profundidade);
					}
					else
					{
						value -= possiveisconexoes(i, j) * (maxprofundidade - this.profundidade);
					}
				}
			}
		}
		return value;
	}
	
	public int possiveisconexoes(int i, int j)
	{
		int value = 0;
		value += linhadequatro(i, j, -1, -1);
		value += linhadequatro(i, j, -1, 0);
		value += linhadequatro(i, j, -1, 1);
		value += linhadequatro(i, j, 0, -1);
		value += linhadequatro(i, j, 0, 1);
		value += linhadequatro(i, j, 1, -1);
		value += linhadequatro(i, j, 1, 0);
		value += linhadequatro(i, j, 1, 1);
		
		return value;
	}
	
	public int linhadequatro(int x, int y, int i, int j)/*para formar a linha de quatro vai buscar os valores e definir como variaveis*/
	{
		int value = 1;
		Piece color = slots[x][y].peca;
		
		for(int k = 1; k < 4; k++)
		{
			if(x+i*k < 0 || y+j*k < 0 || x+i*k >= slots.length || y+j*k >= slots[0].length)
				return 0;
			if(slots[x+i*k][y+j*k].peca == color)
				value++;
			else if (slots[x+i*k][y+j*k].peca != Piece.None)
				return 0;
			else
			{
				for(int l = y+j*k; l >= 0; l--)
					if(slots[x+i*k][l].peca == Piece.None)
						value--;
			}
		}
		
		if(value == 4) return 100;
		if(value < 0) return 0;
		return value;
	}
}

public class ConnectFour extends JFrame implements ActionListener
{
	JLabel lblPlayer = new JLabel("Jogador: ");
	JLabel lblCurrentPlayer = new JLabel("Blue");
	JPanel pnlMenu = new JPanel();
	JPanel pnlSlots = new JPanel();
	JButton btnNewGame = new JButton("Novo Jogo (2 Jogadores)");
	JButton btnNewGame2 = new JButton("Novo Jogo (Inimigo: IA)");
	
	Slot[][] slots = new Slot[7][6];
	
	boolean vencedor = false;
	int jogador = 1;
	boolean Inteligencia;
	
	public ConnectFour(boolean Inteligencia)
	{
		super("4 em Linha!!!");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		jogador = (int)(Math.random()*2) + 1;

		this.Inteligencia = Inteligencia;/*jogar contra o computador*/
		
		btnNewGame.addActionListener(this);
		btnNewGame2.addActionListener(this);
		switch(jogador)
		{
			case 1:
				lblCurrentPlayer.setForeground(Color.blue);
				lblCurrentPlayer.setText("Azul");
				break;
			case 2:
				lblCurrentPlayer.setForeground(Color.red);
				lblCurrentPlayer.setText("Vermelho");
				break;
		}
		pnlMenu.add(btnNewGame);
		pnlMenu.add(btnNewGame2);
		pnlMenu.add(lblPlayer);
		pnlMenu.add(lblCurrentPlayer);
		
		pnlSlots.setLayout(new GridLayout(6, 7));
		
		for(int j = 0; j < 6; j++)
			for(int i = 0; i < 7; i++)
			{
				slots[i][j] = new Slot(i, j);
				slots[i][j].addActionListener(this);
				pnlSlots.add(slots[i][j]);
			}
			
		add(pnlMenu, BorderLayout.NORTH);
		add(pnlSlots, BorderLayout.CENTER);	
		setSize(500, 500);
		setVisible(true);
		
		if(jogador == 2 && Inteligencia) insertTo(minimax());
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == btnNewGame)
		{
			if(JOptionPane.showConfirmDialog(this, "Começar novo jogo?", "SIM", JOptionPane.YES_NO_OPTION) == 0)
			{
				dispose();
				new ConnectFour(false);
				return;
			}
		}
		if(ae.getSource() == btnNewGame2)
		{
			if(JOptionPane.showConfirmDialog(this, "Começar novo jogo contra o computador", "SIM", JOptionPane.YES_NO_OPTION) == 0)
			{
				dispose();
				new ConnectFour(true);
				return;
			}
		}
		else if(!vencedor)
		{
			Slot slot = (Slot)ae.getSource();
			insertTo(slot);
		}
	}
	
	void insertTo(Slot slot)
	{
		if(slot.peca != Piece.None)
			return;
		
		int i = slot.i;
		int j = slot.j;
		
		while(j < slots[0].length-1 && slots[i][j+1].peca == Piece.None)
			j++;
			
		switch(jogador)
		{
			case 1:
				slots[i][j].setPiece(Piece.Blue);
				break;
			case 2:
				slots[i][j].setPiece(Piece.Red);
				break;
		}
		
		jogador = (jogador % 2) + 1;
		
		if(vencedor())/*caso haja um vencedor que faça o 4 em linha o jogo ira acabar */
		{
			lblPlayer.setText("Vencedor: ");
			vencedor = true;
		}
		else
		{
			switch(jogador)/*caso nao haja um vencedor o jogo ira terminar até haver um vencedor ou que acabe o numero possivel de jogadas */
			{
				case 1:
					lblCurrentPlayer.setForeground(Color.blue);
					lblCurrentPlayer.setText("Azul");
					break;
				case 2:
					lblCurrentPlayer.setForeground(Color.red);
					lblCurrentPlayer.setText("Vermelho");
					break;
			}
			
			if(jogador == 2 && Inteligencia)
			{
				insertTo(minimax());
			}
		}
	}
	
	public boolean vencedor() /*o vencedor possivel tem que acabar o jogo num maximo com 6 jogadas verticais caso nao acabe o jogo*/
	{
		for(int j = 0; j < 6; j++)
		{
			for(int i = 0; i < 7; i++)/*o jogo ira acabar por si se chegar a 7º combinacao possivel*/

			{
				if(slots[i][j].peca != Piece.None && combinacoes(i, j))
					return true;
			}
		}
		return false;
	}
	
	public boolean combinacoes(int i, int j)/*sendo o i e j os jogadores,os numeros sao as posicoes que se pode ocupar*/
	{
		if(linhadequatro(i, j, -1, -1))
			return true;
		if(linhadequatro(i, j, -1, 0))
			return true;
		if(linhadequatro(i, j, -1, 1))
			return true;
		if(linhadequatro(i, j, 0, -1))
			return true;
		if(linhadequatro(i, j, 0, 1))
			return true;
		if(linhadequatro(i, j, 1, -1))
			return true;
		if(linhadequatro(i, j, 1, 0))
			return true;
		if(linhadequatro(i, j, 1, 1))
			return true;
		return false;
	}
	
	public boolean linhadequatro(int x, int y, int i, int j)/* cada vez que o jogador joga uma peca o codigo ira verificar se estao  */
	{
		Piece color = slots[x][y].peca;/*cada jogador ira jogar a vez */
		
		for(int k = 1; k < 4; k++)/*a jogada acaba ate se verificar que esta uma linha de 4 completa*/
		{
			if(x+i*k < 0 || y+j*k < 0 || x+i*k >= slots.length || y+j*k >= slots[0].length)
				return false;
			if(slots[x+i*k][y+j*k].peca != color)
				return false;
		}
		return true;
	}
	
	public Slot minimax()
	{
		Arvore tree = new Arvore(slots, 0);
		return slots[tree.getX()][0];
	}
	
	public static void main(String[] args)
	{
		new ConnectFour(false);
	}
}