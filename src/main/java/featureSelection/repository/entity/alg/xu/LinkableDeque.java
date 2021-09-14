package featureSelection.repository.entity.alg.xu;

import java.util.Iterator;
import java.util.Collection;

public class LinkableDeque<Item> implements Deque<Item> ,Iterable<Item>, Cloneable {
	Node<Item> head;
	Node<Item> end;
	int linkedTime;
	private int size;

	@SuppressWarnings("unchecked")
	public LinkableDeque(Item...item) {
		//Initiate end.
		Node<Item> temp = new Node<Item>(item[item.length-1],null,null);
		end = temp;
		for ( int i=item.length-2 ; i>=0 ; i-- )	temp = new Node<Item>(item[i], null, temp);
		//Initiate head
		head = temp;
		
		while ( temp.next !=null ){
			temp.next.previous = temp;
			temp = temp.next;
		}

		size = item.length;
		linkedTime = 0;
	}
	public LinkableDeque() {
		head = end = null;
		linkedTime = size = 0;
	}
	
	/**
	 * Get if the queue is empty.
	 */
	public boolean isEmpty(){
		return size==0;
	}

	/**
	 * Get the size of the queue
	 */
	public int size(){
		return size;
	}

	/**
	 * Get the linked time
	 * 
	 * @return an int value
	 */
	public int linkedTime() {
		return linkedTime;
	}
	
	/**
	 * Add an element at the head of the queue
	 * 
	 * @param item
	 * 		The element to be added with 
	 */
	public void addFirst(Item item){
		Node<Item> node = new Node<Item>(item,null,head);
		head = node;
		size++;

		if ( size==1 )	end = head;
	}

	/**
	 * Add an element at the end of the queue
	 * 
	 * @param item
	 * 		The element to be added with 
	 */
	public void addLast(Item item){
		Node<Item> node = new Node<Item>(item,end,null);
		if (end!=null)		end.next = node;
		else 				head = node;
		end = node;
		size++;
	}

	/**
	 * Get the element at the head of the queue 
	 * and remove it from the queue
	 * 
	 * @return the element./null if the queue is empty.
	 */
	public Item popFirst(){
		if ( size>0 ){
			Item item = head.data;
			if ( head.next!=null )	head = head.next;
			else					end = head = null;
			size--;
			return item;
		}else{
			return null;
		}
	}

	/**
	 * Get the element at the end of the queue 
	 * and remove it from the queue
	 * 
	 * @return the element./null if the queue is empty.
	 */
	public Item popLast(){
		if ( size>0 ){
			Item item = end.data;
			if ( end != head ){
				end = end.previous;
				end.next = null;
			}else{
				head = end = null;
			}
			size--;
			return item;
		}else{
			return null;
		}
	}

	/**
	 * Reset the queue.
	 */
	public void clear() {
		head = end = null;
		linkedTime = size = 0;
	}
	
	/**
	 * Link with another queue.
	 * 
	 * @param deque
	 * 		The collection to be added with
	 */
	public void addAllDeque(LinkableDeque<Item> deque) {
		if ( deque==null || deque.size()==0 )	return;
		if (this.size!=0) {
			this.end.next = deque.head;
			deque.head.previous = this.end;
			this.end = deque.end;
				
			size +=deque.size();
			linkedTime += deque.linkedTime+1;
			deque.clear();
		}else {
			Node<Item> pointer = deque.head;
			while  (pointer!=null){
				addLast(pointer.data);
				pointer = pointer.next;
			}
		}
	}
	
	/**
	 * Add all elements in collection
	 * 
	 * @param collection
	 * 		The collection to be added with
	 */
	public void addAll(Collection<Item> collection) {
		Iterator<Item> iterator = collection.iterator();
		while ( iterator.hasNext() )
			addLast(iterator.next());
	}
	
	/**
	 * Check if contains element
	 * 
	 * @param o
	 * 		The value of the element
	 * @return true if contains.
	 */
	public boolean contains(Object o) {
		Node<Item> n = head;
		while ( n!=null )
			if ( (o==null && n.data==null)|| o!=null && n.data.equals(o) )
				return true;
			else	n = n.next;
		return false;
	}

	/**
	 * Show the previous Node
	 * 
	 * @param node
	 * 		A Node instance
	 */
	public void showPrevious(Node<Item> node){
		if ( node != null ){
			System.out.print("·Node("+node.data+")'s former node is ");
			if ( node.previous!=null )
				System.out.println("Node("+node.previous.data+")");
			else
				System.out.println("null");
		}else {
			System.out.println("·this node is null.");
		}
	}

	/**
	 * Show the next Node
	 * 
	 * @param node
	 * 		A Node instance
	 */
	public void showNext(Node<Item> node){
		if ( node != null ){
			System.out.print("·Node("+node.data+")'s next node is ");
			if ( node.next!=null )
				System.out.println("Node("+node.next.data+")");
			else
				System.out.println("null");
		}else {
			System.out.println("·this node is null.");
		}
	}

	/**
	 * Deep clone of the MyDeque
	 * 
	 * @return a clone of the MyDeque
	 */
	public LinkableDeque<Item> clone(){
		LinkableDeque<Item> clone = new LinkableDeque<>();
		Node<Item> pointer = head;

		while  (pointer!=null){
			clone.addLast(pointer.data);
			pointer = pointer.next;
		}

		return clone;
	}

	/**
	 * Show elements of the queue
	 */
	public void display(){
		Node<Item> temp = head;
		System.out.print("·Deque display :\n	data.....");
		while ( temp != null ){
			System.out.print(temp.data+" ");
			temp=temp.next;
		}
		System.out.println("\n	size....."+size);
		if ( head != null && size>0 )
			System.out.println("	type....."+head.data.getClass().getName());
	}

	/**
	 * Get an array of the queue's elements
	 * 
	 * @return an Object array
	 */
	public Object[] toArray(){
		Object[] result = new Object[size];
		Node<Item> pointer = head;
		if (pointer==null)	return result;
		
		int i=0;
		while (pointer!=null){
			result[i++] = (Item)pointer.data;
			pointer = pointer.next;
		}
		return result;
	}
	
	/**
	 * Get an array of the queue's elements
	 * 
	 * @param array
	 * 		A new array instance with the queue's size
	 * @return an Object array
	 */
	public Item[] toArray(Item[] array){
		if ( array.length!=size )
			throw new IllegalArgumentException("Wrong array size!");
		Node<Item> pointer = head;
		if (pointer==null)	return null;
		int i=0;

		while (pointer!=null){
			array[i++] = pointer.data;
			pointer = pointer.next;
		}

		return array;
	}

	public Iterator<Item> iterator(){
		return new ListIterator();
	}
	
	public class Node<E> {
		E data;
		Node<E> previous;
		Node<E> next;

		public Node(E data,Node<E> previous, Node<E> next) {
			this.data = data;
			this.previous = previous;
			this.next = next;
		}

		public Node(){
			this(null, null, null);
		}
	}

	private class ListIterator implements Iterator<Item>{
		private Node<Item> current = head;

		public boolean hasNext(){
			return current != null;
		}

		public void remove(){		}

		public Item next(){
			Item item = current.data;
			current = current.next;
			return item;
		}
	}

	public static void main(String[] args) {
		/*MyDeque d = new MyDeque();
		d.display();
		d.showPrevious(d.head);
		d.showNext(d.head);
		d.display();
		d.pushLeft(2);
		d.pushRight(0);
		d.popLeft();
		d.popRight();
		d.popRight();*/

		Integer[] num = new Integer[9];
		for (int i=0; i<num.length; i++)
			num[i] = 10+1+i;

		LinkableDeque<Integer> queue1 = new LinkableDeque<>(num);
		queue1.display();
		for (int i=0; i<num.length; i++)
			num[i] = 20+1+i;
		LinkableDeque<Integer> queue2 = new LinkableDeque<>(num);
		queue1.addAllDeque(queue2);
		queue1.display();
	}
}